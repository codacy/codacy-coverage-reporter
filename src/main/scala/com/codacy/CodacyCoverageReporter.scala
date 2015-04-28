package com.codacy

import java.io.File

import ch.qos.logback.classic.{Level, Logger}
import com.codacy.api.Language
import com.codacy.api.client.CodacyClient
import com.codacy.api.helpers.FileHelper
import com.codacy.api.service.CoverageServices
import com.codacy.parsers._
import org.slf4j.LoggerFactory
import scopt.Read

object CodacyCoverageReporter {

  private val publicApiBaseUrl = "https://www.codacy.com"
  private val rootProjectDir = new File(System.getProperty("user.dir"))

  private val logger = {
    val logger = LoggerFactory.getLogger("com.codacy.CodacyCoverageReporter").asInstanceOf[Logger]
    logger
  }

  case class Config(language: Language.Value = Language.Python,
                    projectToken: String = getProjectToken,
                    coverageReport: File = new File("coverage.xml"),
                    codacyApiBaseUrl: String = getApiBaseUrl,
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

  def main(args: Array[String]): Unit = {

    val parser = new scopt.OptionParser[Config]("codacy-coverage-reporter") {
      head("codacy-coverage-reporter", "1.0.0")
      opt[Language.Value]('l', "language").required().action { (x, c) =>
        c.copy(language = x)
      }.text("foo is an integer property")
      opt[String]('t', "projectToken").optional().action { (x, c) =>
        c.copy(projectToken = x)
      }.text("your project API token")
      opt[File]('r', "coverageReport").required().action { (x, c) =>
        c.copy(coverageReport = x)
      }.text("your project API token")
      opt[String]("codacyApiBaseUrl").optional().action { (x, c) =>
        c.copy(codacyApiBaseUrl = x)
      }.text("the base URL for the Codacy API")
      opt[Unit]("debug").optional().hidden().action { (_, c) =>
        c.copy(debug = true)
      }
      help("help").text("prints this usage text")
    }

    parser.parse(args, Config()) match {
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

  def codacyCoverage(config: Config): Unit = {
    FileHelper.withTokenAndCommit(Some(config.projectToken)) {
      case (projectToken, commitUUID) =>

        logger.debug(s"Project token: $projectToken")
        logger.info(s"Parsing coverage data...")

        //TODO: Check if config.coverageReport exists

        CoverageParserFactory.withCoverageReport(config.language, rootProjectDir, config.coverageReport) {
          report =>
            val codacyReportFilename = s"${config.coverageReport.getParent}${File.separator}codacy-coverage.json"
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
        }.joinRight

    } match {
      case Left(error) =>
        logger.error(error)
        System.exit(1)
      case Right(message) =>
        logger.info(message)
        System.exit(0)
    }
  }

}
