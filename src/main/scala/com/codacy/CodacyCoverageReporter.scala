package com.codacy

import java.io.File
import java.net.URL

import ch.qos.logback.classic.{Level, Logger}
import com.codacy.api.client.CodacyClient
import com.codacy.api.helpers.FileHelper
import com.codacy.api.service.CoverageServices
import com.codacy.api.{CoverageReport, Language}
import com.codacy.parsers.CoverageParserFactory
import com.codacy.transformation.PathPrefixer
import org.slf4j.LoggerFactory
import scopt.Read

import scala.util.Try

object CodacyCoverageReporter {

  private val publicApiBaseUrl = "https://api.codacy.com"
  private val rootProjectDir = new File(System.getProperty("user.dir"))

  private val logger = {
    val logger = LoggerFactory.getLogger("com.codacy.CodacyCoverageReporter").asInstanceOf[Logger]
    logger
  }

  case class Config(language: Language.Value = Language.Python,
                    projectToken: String = getProjectToken,
                    coverageReport: File = new File("coverage.xml"),
                    codacyApiBaseUrl: String = getApiBaseUrl,
                    prefix: String = "",
                    debug: Boolean = false)

  implicit def languageRead: Read[Language.Value] = Read.reads { (s: String) =>
    Language.withName(s)
  }

  private def getApiBaseUrl: String = {
    sys.env.getOrElse("CODACY_API_BASE_URL", publicApiBaseUrl)
  }

  private def getProjectToken: String = {
    sys.env.getOrElse("CODACY_PROJECT_TOKEN", "")
  }

  private def validUrl(baseUrl: String) = {
    Try(new URL(baseUrl)).toOption.isDefined
  }

  def main(args: Array[String]): Unit = {

    val parser = buildParser

    parser.parse(args, Config()) match {
      case Some(config) if !validUrl(config.codacyApiBaseUrl) =>
        logger.error(s"Error: Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}")
        if(!config.codacyApiBaseUrl.startsWith("http")) {
          logger.error("Maybe you forgot the http:// or https:// ?")
        }

      case Some(config) if config.projectToken.trim.nonEmpty =>
        if (config.debug) {
          logger.setLevel(Level.DEBUG)
        }
        logger.debug(config.toString)

        codacyCoverage(config)
      case Some(config) if config.projectToken.trim.isEmpty =>
        logger.error("Error: Missing option --projectToken")
      case _ =>
    }

  }

  def buildParser = {
    new scopt.OptionParser[Config]("codacy-coverage-reporter") {
      head("codacy-coverage-reporter", getClass.getPackage.getImplementationVersion)
      opt[Language.Value]('l', "language").required().action { (x, c) =>
        c.copy(language = x)
      }.text("your project language")
      opt[String]('t', "projectToken").optional().action { (x, c) =>
        c.copy(projectToken = x)
      }.text("your project API token")
      opt[File]('r', "coverageReport").required().action { (x, c) =>
        c.copy(coverageReport = x)
      }.text("your project coverage file name")
      opt[String]("codacyApiBaseUrl").optional().action { (x, c) =>
        c.copy(codacyApiBaseUrl = x)
      }.text("the base URL for the Codacy API")
      opt[String]("prefix").optional().action { (x, c) =>
        c.copy(prefix = x)
      }.text("the project path prefix")
      opt[Unit]("debug").optional().hidden().action { (_, c) =>
        c.copy(debug = true)
      }
      help("help").text("prints this usage text")
    }
  }

  def coverageWithTokenAndCommit(config: Config): Either[String, String] = {
    val commitUUID =
      getNonEmptyEnv("CI_COMMIT") orElse
        getNonEmptyEnv("TRAVIS_PULL_REQUEST_SHA") orElse
        getNonEmptyEnv("TRAVIS_COMMIT") orElse
        getNonEmptyEnv("DRONE_COMMIT") orElse
        getNonEmptyEnv("CIRCLE_SHA1") orElse
        getNonEmptyEnv("CI_COMMIT_ID") orElse
        getNonEmptyEnv("WERCKER_GIT_COMMIT")
          .filter(_.trim.nonEmpty)

    FileHelper.withTokenAndCommit(Some(config.projectToken), commitUUID) {
      case (projectToken, commitUUID) =>

        logger.debug(s"Project token: $projectToken")
        logger.info(s"Parsing coverage data...")

        //TODO: Check if config.coverageReport exists

        CoverageParserFactory.withCoverageReport(config.language, rootProjectDir, config.coverageReport)(transform(_)(config) {
          report =>
            val codacyReportFilename = s"${config.coverageReport.getAbsoluteFile.getParent}${File.separator}codacy-coverage.json"
            logger.debug(s"Saving parsed report to $codacyReportFilename")
            val codacyReportFile = new File(codacyReportFilename)

            logger.debug(report.toString)
            FileHelper.writeJsonToFile(codacyReportFile, report)

            val codacyClient = new CodacyClient(Some(config.codacyApiBaseUrl), projectToken = Some(projectToken))
            val coverageServices = new CoverageServices(codacyClient)

            logger.info(s"Uploading coverage data...")

            coverageServices.sendReport(commitUUID, config.language, report) match {
              case requestResponse if requestResponse.hasError =>
                Left(s"Failed to upload report: ${requestResponse.message}")
              case requestResponse =>
                Right(s"Coverage data uploaded. ${requestResponse.message}")
            }
        }).joinRight
    }
  }

  def codacyCoverage(config: Config): Unit = {
    coverageWithTokenAndCommit(config) match {
      case Left(error) =>
        logger.error(error)
        System.exit(1)
      case Right(message) =>
        logger.info(message)
        System.exit(0)
    }
  }

  private def getNonEmptyEnv(key: String): Option[String] = {
    sys.env.get(key).filter(_.trim.nonEmpty)
  }

  private def transform[A](report: CoverageReport)(config: Config)(f: CoverageReport => A): A = {
    val transformations = Set(new PathPrefixer(config.prefix))
    val transformedReport = transformations.foldLeft(report) {
      (report, transformation) => transformation.execute(report)
    }

    f(transformedReport)
  }

}
