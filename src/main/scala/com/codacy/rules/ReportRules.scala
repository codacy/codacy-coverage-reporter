package com.codacy.rules

import java.io.File

import cats.implicits._
import com.codacy.api.client.{FailedResponse, SuccessfulResponse}
import com.codacy.api.helpers.FileHelper
import com.codacy.api.helpers.vcs.{CommitInfo, GitClient}
import com.codacy.api.service.CoverageServices
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{BaseConfig, Configuration, FinalConfig, ReportConfig}
import com.codacy.parsers.CoverageParserFactory
import com.codacy.transformation.PathPrefixer
import org.log4s.Logger
import rapture.json.jsonBackends.play._
import rapture.json.{Json, Serializer}

class ReportRules(config: Configuration,
                  coverageServices: => CoverageServices) {

  private val logger: Logger = LoggerHelper.logger(getClass, config)

  private val rootProjectDir = new File(System.getProperty("user.dir"))

  def codacyCoverage(config: ReportConfig): Either[String, String] = {

    if (!config.coverageReport.exists()) {
      s"File ${config.coverageReport.getAbsolutePath} does not exist.".asLeft
    } else if (!config.coverageReport.canRead) {
      s"Missing read permissions for report file: ${config.coverageReport.getAbsolutePath}".asLeft
    } else {
      coverageWithTokenAndCommit(config) match {
        case Left(error) =>
          error.asLeft
        case Right(message) =>
          message.asRight
      }
    }
  }

  def finalReport(config: FinalConfig): Either[String, String] = {
    withCommitUUID(config.baseConfig) { commitUUID =>
      coverageServices.sendFinalNotification(commitUUID) match {
        case SuccessfulResponse(value) =>
          Right(s"Final coverage notification sent. ${value.success}")
        case FailedResponse(message) =>
          Left(s"Failed to send final coverage notification: $message")
      }
    }
  }

  private def withCommitUUID[T](config: BaseConfig
                               )(block: (String) => Either[String, T]
                               ): Either[String, T] = {
    val maybeCommitUUID = config.commitUUID.fold {
      val currentPath = new File(System.getProperty("user.dir"))
      new GitClient(currentPath).latestCommitInfo
        .fold(
          "Commit UUID not provided and could not retrieve it from current directory ".asLeft[String]
        ) { case CommitInfo(uuid, authorName, authorEmail, date) =>
          val info =
            s"""Commit UUID not provided, using latest commit of current directory:
               |$uuid $authorName <$authorEmail> $date""".stripMargin
          logger.info(info)

          uuid.asRight[String]
        }
    }(_.asRight)

    maybeCommitUUID.flatMap(block)
  }

  private[rules] def coverageWithTokenAndCommit(config: ReportConfig): Either[String, String] = {
    withCommitUUID(config.baseConfig) { commitUUID =>

      logger.debug(s"Project token: ${config.baseConfig.projectToken}")
      logger.info(s"Parsing coverage data from: ${config.coverageReport.getAbsolutePath} ...")

      CoverageParserFactory.withCoverageReport(config.language, rootProjectDir, config.coverageReport)(transform(_)(config) {
        case report if report.fileReports.isEmpty =>
          Left("The provided coverage report generated an empty result.")

        case report =>
          val codacyReportFilename = s"${config.coverageReport.getAbsoluteFile.getParent}${File.separator}codacy-coverage.json"
          logger.debug(s"Saving parsed report to $codacyReportFilename")
          val codacyReportFile = new File(codacyReportFilename)

          logger.debug(report.toString)
          implicit val ser = implicitly[Serializer[CoverageFileReport, Json]]
          FileHelper.writeJsonToFile(codacyReportFile, report)

          logUploadedFileInfo(codacyReportFile)

          coverageServices.sendReport(commitUUID, config.languageStr, report) match {
            case SuccessfulResponse(value) =>
              Right(s"Coverage data uploaded. ${value.success}")
            case failed: FailedResponse =>
              val message = handleFailedResponse(failed)
              Left(s"Failed to upload report: $message")
          }
      }).joinRight
    }
  }

  private def logUploadedFileInfo(codacyReportFile: File): Unit = {
    // Convert to kB with 2 decimal places
    val fileSize = ((codacyReportFile.length / 1024.0) * 100).toInt / 100.0
    val filePath = codacyReportFile.getAbsolutePath

    logger.info(s"Generated report: $filePath ($fileSize kB)")
    logger.info("Uploading coverage data...")
  }

  private def handleFailedResponse(response: FailedResponse): String = {
    if (response.message.contains("not found")) {
      """Request URL not found. (Check if the project token or the API base URL are valid)"""
    } else {
      response.message
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
