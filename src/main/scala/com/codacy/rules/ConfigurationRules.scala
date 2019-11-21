package com.codacy.rules

import java.net.URL
import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, CommandConfiguration, Final, Report}
import com.codacy.model.configuration.{BaseConfig, Configuration, FinalConfig, ReportConfig}
import com.typesafe.scalalogging.StrictLogging

import scala.util.Try
import com.codacy.model.configuration.CommitUUID

class ConfigurationRules(cmdConfig: CommandConfiguration) extends StrictLogging {
  private[rules] val publicApiBaseUrl = "https://api.codacy.com"

  lazy val validatedConfig: Configuration = {
    val config = validateConfig(cmdConfig)
    config.fold({ error =>
      logger.error(s"Invalid configuration: $error")
      sys.exit(1)
    }, identity)
  }

  private def validateConfig(cmdConfig: CommandConfiguration): Either[String, Configuration] = {
    cmdConfig match {
      case config: Report =>
        logger.debug("Parsing report command config")
        validateReportConfig(config)
      case config: Final =>
        logger.debug("Parsing final command config")
        validateFinalConfig(config)
    }
  }

  private def validateFinalConfig(finalConfig: Final): Either[String, FinalConfig] = {
    for {
      baseConfig <- validateBaseConfig(finalConfig.baseConfig)
    } yield FinalConfig(baseConfig)
  }

  private def validateReportConfig(reportConfig: Report): Either[String, ReportConfig] = {
    def validate(reportConf: ReportConfig) = {
      reportConf match {
        case config if config.language.isEmpty && config.languageOpt.isDefined && !config.forceLanguage =>
          Left(s"Invalid language ${config.languageOpt.get}")

        case _ =>
          Right(reportConf)
      }
    }

    for {
      baseConfig <- validateBaseConfig(reportConfig.baseConfig)
      validReportFiles <- validateReportFiles(reportConfig.coverageReports)
      reportConf = ReportConfig(
        baseConfig,
        reportConfig.language,
        reportConfig.forceLanguageValue,
        validReportFiles,
        reportConfig.partialValue,
        reportConfig.prefix.getOrElse("")
      )
      validatedConfig <- validate(reportConf)
    } yield validatedConfig

  }

  private def validateBaseConfig(baseConfig: BaseCommandConfig): Either[String, BaseConfig] = {
    for {
      projectToken <- baseConfig.projectToken.fold(getProjectToken(sys.env, baseConfig.skipValue))(Right(_))
      baseConf = BaseConfig(
        projectToken,
        baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl(sys.env)),
        baseConfig.commitUUID.map(CommitUUID(_)),
        baseConfig.debugValue
      )
      validatedConfig <- {
        baseConf match {
          case config if !validUrl(config.codacyApiBaseUrl) =>
            val error = s"Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}"

            val help = if (!config.codacyApiBaseUrl.startsWith("http")) {
              "Maybe you forgot the http:// or https:// ?"
            }
            Left(s"$error\n$help")

          case config if config.projectToken.trim.isEmpty =>
            Left("Empty argument for --project-token")

          case _ =>
            Right(baseConf)
        }
      }
    } yield {
      logger.info(s"Using API base URL: ${validatedConfig.codacyApiBaseUrl}")
      validatedConfig
    }
  }

  /**
    * Get API base URL
    *
    * This function try to get the API base URL from environment variables, and if not
    * found, fallback to the public API base URL
    * @param envVars environment variables
    * @return api base url
    */
  private[rules] def getApiBaseUrl(envVars: Map[String, String]): String = {
    envVars.getOrElse("CODACY_API_BASE_URL", publicApiBaseUrl)
  }

  /**
    * Get project token
    *
    * This function try to get the project token from environment variables, and if not found
    * return an error message. If the skip flag is true, it will exit the reporter with the normal state
    * skipping the coverage to be reported.
    * @param envVars environment variables
    * @param skip skip flag
    * @return the project token on the right or an error message on the left
    */
  private[rules] def getProjectToken(envVars: Map[String, String], skip: Boolean): Either[String, String] = {
    val projectToken =
      envVars
        .get("CODACY_PROJECT_TOKEN")
        .toRight("Project token not provided and not available in environment variable \"CODACY_PROJECT_TOKEN\"")

    if (skip && projectToken.isLeft) {
      logger.warn(projectToken.left.get)
      logger.info("Skip reporting coverage")
      sys.exit(0)
    } else {
      projectToken
    }
  }

  /**
    * Validate an URL
    *
    * This function check if the url is valid or not
    * @param baseUrl base url
    * @return true for valid url, false if not
    */
  private[rules] def validUrl(baseUrl: String): Boolean = {
    Try(new URL(baseUrl)).toOption.isDefined
  }

  /**
    * Validate report files option
    *
    * This function check if the report files option is valid or not.
    * @param filesOpt files option
    * @return list of files if validated on the right or an error message if not on the left
    */
  private[rules] def validateReportFiles(filesOpt: Option[List[File]]): Either[String, List[File]] = {
    filesOpt match {
      case Some(value) if value.isEmpty =>
        Left("Invalid report list. Try passing a report file with -r")
      case Some(value) if !value.isEmpty =>
        Right(value)
      case None =>
        Right(List.empty[File])
    }
  }
}
