package com.codacy.rules

import java.io.File
import java.nio.file.Files

import cats.implicits._
import com.codacy.api.CoverageReport
import com.codacy.api.client.{FailedResponse, SuccessfulResponse}
import com.codacy.api.helpers.FileHelper
import com.codacy.api.service.CoverageServices
import com.codacy.model.configuration.{BaseConfig, Configuration, FinalConfig, ReportConfig}
import com.codacy.parsers.CoverageParser
import com.codacy.transformation.PathPrefixer
import com.typesafe.scalalogging.StrictLogging
import com.codacy.plugins.api.languages.Languages

import com.codacy.rules.commituuid.CommitUUIDProvider

import scala.collection.JavaConverters._

class ReportRules(config: Configuration, coverageServices: => CoverageServices) extends StrictLogging {

  private val rootProjectDir = new File(System.getProperty("user.dir"))
  private val rootProjectDirIterator = Files
    .walk(rootProjectDir.toPath())
    .iterator()
    .asScala
    .map(_.toFile)

  def codacyCoverage(config: ReportConfig): Either[String, String] = {
    coverageWithTokenAndCommit(config) match {
      case Left(error) =>
        error.asLeft
      case Right(message) =>
        message.asRight
    }
  }

  private[rules] def coverageWithTokenAndCommit(config: ReportConfig): Either[String, String] = {
    withCommitUUID(config.baseConfig) { commitUUID =>
      logger.debug(s"Project token: ${config.baseConfig.projectToken}")

      val files = guessReportFiles(config.coverageReports, rootProjectDirIterator)

      files
        .fold(
          _.asLeft,
          files => {
            val finalConfig = if (files.length > 1 && !config.partial) {
              logger.info("More than one file. Considering a partial report")
              config.copy(partial = true)
            } else {
              config
            }

            files
              .map {
                case file if (!file.exists) =>
                  s"File ${file.getAbsolutePath} does not exist.".asLeft
                case file if (!file.canRead) =>
                  s"Missing read permissions for report file: ${file.getAbsolutePath}".asLeft
                case file =>
                  logger.info(s"Parsing coverage data from: ${file.getAbsolutePath} ...")

                  CoverageParser
                    .parse(rootProjectDir, file)
                    .map(transform(_)(finalConfig))
                    .flatMap {
                      case report if report.fileReports.isEmpty =>
                        Left("The provided coverage report generated an empty result.")

                      case report =>
                        val codacyReportFilename =
                          s"${file.getAbsoluteFile.getParent}${File.separator}codacy-coverage.json"
                        logger.debug(s"Saving parsed report to $codacyReportFilename")
                        val codacyReportFile = new File(codacyReportFilename)

                        logger.debug(report.toString)
                        FileHelper.writeJsonToFile(codacyReportFile, report)

                        logUploadedFileInfo(codacyReportFile)

                        val language = guessReportLanguage(finalConfig.languageOpt, report)

                        language.map(languageStr => {
                          coverageServices.sendReport(commitUUID, languageStr, report, finalConfig.partial) match {
                            case SuccessfulResponse(value) =>
                              logger.info(s"Coverage data uploaded. ${value.success}")
                              Right(())
                            case failed: FailedResponse =>
                              val message = handleFailedResponse(failed)
                              Left(s"Failed to upload report: $message")
                          }
                        })
                    }
              }
              .collectFirst {
                case Left(l) => Left(l)
              }
              .getOrElse(Right(s"All coverage data uploaded."))
          }
        )
    }
  }

  /**
    * Guess report language
    *
    * This function try to guess the report language using the first filename on
    * the report file.
    * @param languageOpt language option provided by the config
    * @param report coverage report
    * @return the guessed language name on the right or an error on the left.
    */
  private[rules] def guessReportLanguage(
      languageOpt: Option[String],
      report: CoverageReport
  ): Either[String, String] = {
    languageOpt match {
      case Some(l) => Right(l)
      case None =>
        report.fileReports.headOption match {
          case None => Left("Can't guess the language due to empty report")
          case Some(fileReport) =>
            Languages.forPath(fileReport.filename) match {
              case None => Left("Can't guess the language due invalid path")
              case Some(value) => Right(value.toString)
            }
        }
    }
  }

  /**
    * Guess the report file
    *
    * This function try to guess the report language based on common report file names.
    * @param files coverage file option provided by the config
    * @param pathIterator path iterator to search the files
    * @return the guessed report files on the right or an error on the left.
    */
  private[rules] def guessReportFiles(files: List[File], pathIterator: Iterator[File]): Either[String, List[File]] = {
    val JacocoRegex = """(jacoco.*\.xml)""".r
    val CoberturaRegex = "(cobertura.xml)".r
    val LCOVRegex = """(lcov(.info|.dat)|.*\.lcov)""".r

    files match {
      case value if value.isEmpty =>
        val foundFiles = pathIterator
          .filter(_.getName() match {
            case JacocoRegex(_) | CoberturaRegex(_) | LCOVRegex(_) => true
            case _ => false
          })
          .toList
        if (foundFiles.isEmpty)
          "Can't guess any report due to no matching! Try to specify the report with -r".asLeft
        else
          foundFiles.asRight
      case value =>
        value.asRight
    }
  }

  /**
    * Log uploaded file information
    *
    * This function log some summary information about the uploaded coverage file.
    * @param codacyReportFile coverage report file
    */
  private def logUploadedFileInfo(codacyReportFile: File): Unit = {
    // Convert to kB with 2 decimal places
    val fileSize = ((codacyReportFile.length / 1024.0) * 100).toInt / 100.0
    val filePath = codacyReportFile.getAbsolutePath

    logger.info(s"Generated report: $filePath ($fileSize kB)")
    logger.info("Uploading coverage data...")
  }

  /**
    * Handle failed response
    *
    * This function handle failed response and transform it to a user readable message.
    * @param response failed response
    * @return user readable message
    */
  private[rules] def handleFailedResponse(response: FailedResponse): String = {
    if (response.message.contains("not found")) {
      """Request URL not found. (Check if the project token or the API base URL are valid)"""
    } else {
      response.message
    }
  }

  private def transform(report: CoverageReport)(config: ReportConfig): CoverageReport = {
    val transformations = Set(new PathPrefixer(config.prefix))
    transformations.foldLeft(report) { (report, transformation) =>
      transformation.execute(report)
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

  private def withCommitUUID[T](config: BaseConfig)(block: (String) => Either[String, T]): Either[String, T] = {
    val maybeCommitUUID = config.commitUUID.fold {
      val envVars = sys.env.filter { case (_, value) => value.trim.nonEmpty }
      CommitUUIDProvider.getFromAll(envVars)
    }(_.asRight)

    maybeCommitUUID.flatMap(uuid => block(uuid.value))
  }

}
