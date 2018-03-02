package com.codacy.rules

import java.io.File

import cats.implicits._
import ch.qos.logback.classic.Logger
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.api.client.{CodacyClient, FailedResponse, SuccessfulResponse}
import com.codacy.api.helpers.FileHelper
import com.codacy.api.service.CoverageServices
import com.codacy.model.configuration.{FinalConfig, ReportConfig}
import com.codacy.parsers.CoverageParserFactory
import com.codacy.transformation.PathPrefixer
import rapture.json.{Json, Serializer}
import rapture.json.jsonBackends.play._

class ReportRules(logger: => Logger) {

  private val rootProjectDir = new File(System.getProperty("user.dir"))

  def codacyCoverage(config: ReportConfig): Unit = {
    if (config.coverageReport.exists()) {
      coverageWithTokenAndCommit(config) match {
        case Left(error) =>
          logger.error(error)
          System.exit(1)
        case Right(message) =>
          logger.info(message)
          System.exit(0)
      }
    } else {
      logger.error(s"File ${config.coverageReport.getName} does not exist.")
      System.exit(1)
    }
  }

  def finalReport(config: FinalConfig): Unit = ???


  private[rules] def coverageWithTokenAndCommit(config: ReportConfig): Either[String, String] = {
    FileHelper.withTokenAndCommit(Some(config.baseConfig.projectToken), config.baseConfig.commitUUID) {
      case (projectToken, commitUUID) =>

        logger.debug(s"Project token: $projectToken")
        logger.info(s"Parsing coverage data...")

        CoverageParserFactory.withCoverageReport(config.language, rootProjectDir, config.coverageReport)(transform(_)(config) {
          report =>
            val codacyReportFilename = s"${config.coverageReport.getAbsoluteFile.getParent}${File.separator}codacy-coverage.json"
            logger.debug(s"Saving parsed report to $codacyReportFilename")
            val codacyReportFile = new File(codacyReportFilename)

            logger.debug(report.toString)
            implicit val ser = implicitly[Serializer[CoverageFileReport, Json]]
            FileHelper.writeJsonToFile(codacyReportFile, report)

            val codacyClient = new CodacyClient(Some(config.baseConfig.codacyApiBaseUrl), projectToken = Some(projectToken))
            val coverageServices = new CoverageServices(codacyClient)

            logger.info(s"Uploading coverage data...")

            coverageServices.sendReport(commitUUID, config.languageStr, report) match {
              case SuccessfulResponse(value) =>
                Right(s"Coverage data uploaded. $value")
              case FailedResponse(message) =>
                Left(s"Failed to upload report: $message")
            }
        }).joinRight
    }
  }

  private def transform[A](report: CoverageReport)(config: ReportConfig)(f: CoverageReport => A): A = {
    val transformations = Set(new PathPrefixer(config.prefix))
    val transformedReport = transformations.foldLeft(report) {
      (report, transformation) => transformation.execute(report)
    }

    f(transformedReport)
  }
}
