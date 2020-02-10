package com.codacy.rules

import java.io.File
import java.nio.file.Files

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
    .walk(rootProjectDir.toPath)
    .iterator()
    .asScala
    .map(_.toFile)

  def codacyCoverage(config: ReportConfig): Either[String, String] = {
    withCommitUUID(config.baseConfig) { commitUUID =>
      logger.debug(s"Project token: ${config.baseConfig.projectToken}")

      val filesEither = guessReportFiles(config.coverageReports, rootProjectDirIterator)

      filesEither.flatMap { files =>
        val finalConfig = if (files.length > 1 && !config.partial) {
          logger.info("More than one file. Considering a partial report")
          config.copy(partial = true)
        } else config

        val reportResults = files
          .map {
            case file if !file.exists =>
              Left(s"File ${file.getAbsolutePath} does not exist.")
            case file if !file.canRead =>
              Left(s"Missing read permissions for report file: ${file.getAbsolutePath}")
            case file =>
              logger.info(s"Parsing coverage data from: ${file.getAbsolutePath} ...")

              val result = for {
                report <- CoverageParser.parse(rootProjectDir, file).map(transform(_)(finalConfig))
                _ <- storeReport(report, file.getAbsoluteFile.getParent, finalConfig)
                language <- guessReportLanguage(config.languageOpt, report)
              } yield { sendReport(report, language, finalConfig, commitUUID) }

              result.left.foreach(errorMessage => logger.warn(errorMessage))
              result
          }

        if (reportResults.forall(_.isRight))
          Right("All coverage data uploaded.")
        else if (reportResults.exists(_.isRight))
          Right("Not all files were successfully uploaded.")
        else
          Left("No files were uploaded.")
      }
    }
  }

  /**
    * Store Report
    *
    * Store the parsed report for troubleshooting purposes
    * @param report coverage report to be stored
    * @param path directory to store the report in
    * @param config configuration
    * @return either an error message or nothing
    */
  private def storeReport(report: CoverageReport, path: String, config: ReportConfig) = {
    if (report.fileReports.isEmpty)
      Left("The provided coverage report generated an empty result.")
    else {
      val codacyReportFilename =
        s"$path${File.separator}codacy-coverage.json"
      logger.debug(s"Saving parsed report to $codacyReportFilename")
      val codacyReportFile = new File(codacyReportFilename)

      logger.debug(report.toString)
      FileHelper.writeJsonToFile(codacyReportFile, report)

      logUploadedFileInfo(codacyReportFile)
      Right(())
    }
  }

  /**
    * Send Report
    *
    * Send the parsed report to coverage services with the given language and commitUUID
    * @param report coverage report to be sent
    * @param language language detected in files or specified by user input
    * @param config configuration
    * @param commitUUID unique id of commit being reported
    * @return either an error message or nothing
    */
  private def sendReport(report: CoverageReport, language: String, config: ReportConfig, commitUUID: String) = {
    coverageServices.sendReport(commitUUID, language, report, config.partial) match {
      case SuccessfulResponse(value) =>
        logger.info(s"Coverage data uploaded. ${value.success}")
        Right(())
      case failed: FailedResponse =>
        val message = handleFailedResponse(failed)
        Left(s"Failed to upload report: $message")
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
    val CoberturaRegex = """(cobertura\.xml)""".r
    val LCOVRegex = """(lcov(.info|.dat)|.*\.lcov)""".r
    val CloverRegex = """(clover\.xml)""".r
    val DotcoverRegex = """(dotcover\.xml)""".r
    val OpencoverRegex = """(opencover\.xml)""".r
    val PhpUnitRegex = """(index\.xml)""".r

    files match {
      case value if value.isEmpty =>
        val foundFiles = pathIterator
          .filter(_.getName match {
            case JacocoRegex(_) | CoberturaRegex(_) | LCOVRegex(_) | CloverRegex(_) | DotcoverRegex(_) |
                OpencoverRegex(_) | PhpUnitRegex(_) =>
              true
            case _ => false
          })
          .toList
        if (foundFiles.isEmpty)
          Left("Can't guess any report due to no matching! Try to specify the report with -r")
        else
          Right(foundFiles)
      case value =>
        Right(value)
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

  private def withCommitUUID[T](config: BaseConfig)(block: String => Either[String, T]): Either[String, T] = {
    val maybeCommitUUID = config.commitUUID.map(Right(_)).getOrElse {
      val envVars = sys.env.filter { case (_, value) => value.trim.nonEmpty }
      CommitUUIDProvider.getFromAll(envVars)
    }

    maybeCommitUUID.flatMap(uuid => block(uuid.value))
  }

}
