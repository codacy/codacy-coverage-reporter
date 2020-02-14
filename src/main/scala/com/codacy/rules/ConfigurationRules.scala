package com.codacy.rules

import java.net.URL
import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, CommandConfiguration, Final, Report}
import com.codacy.model.configuration.{
  BaseConfig,
  BaseConfigWithApiToken,
  BaseConfigWithProjectToken,
  CommitUUID,
  Configuration,
  FinalConfig,
  ReportConfig
}
import com.typesafe.scalalogging.StrictLogging

import scala.util.Try

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

  private[rules] def validateBaseConfig(baseConfig: BaseCommandConfig): Either[String, BaseConfig] = {
    val errorMessage =
      "Either a project token or an api token must be provided or available in an environment variable"

    val projectToken = getValueOrEnvironmentVar(baseConfig.projectToken, "CODACY_PROJECT_TOKEN")
    val apiToken = getValueOrEnvironmentVar(baseConfig.apiToken, "CODACY_API_TOKEN")

    baseConfig match {
      case config if projectToken.isDefined => validateConfigWithProjectToken(config, projectToken)
      case config if apiToken.isDefined => validateConfigWithApiToken(config, apiToken)
      case config if config.skipValue =>
        logger.warn(errorMessage)
        logger.info("Skip reporting coverage")
        sys.exit(0)
      case _ => Left(errorMessage)
    }
  }

  private def validateConfigWithProjectToken(baseConfig: BaseCommandConfig, projectToken: Option[String]) = {
    val projectTokenErrorMsg = "Empty argument for --project-token"
    for {
      projectToken <- projectToken.toRight(projectTokenErrorMsg)
      baseConf = BaseConfigWithProjectToken(
        projectToken,
        baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl(sys.env)),
        baseConfig.commitUUID.map(CommitUUID),
        baseConfig.debugValue
      )
      validatedConfig <- validateConfig(baseConf) {
        case config: BaseConfigWithProjectToken if !config.projectToken.trim.isEmpty => Right(config)
        case _ => Left(projectTokenErrorMsg)
      }

    } yield {
      logger.info(s"Using Project token -> API base URL: ${validatedConfig.codacyApiBaseUrl}")
      validatedConfig
    }
  }

  private def validateConfigWithApiToken(baseConfig: BaseCommandConfig, apiToken: Option[String]) = {
    val apiTokenErrorMsg = "Empty argument --api-token"
    val emptyUsernameMsg = "Empty argument --username"
    val emptyProjectMsg = "Empty argument --project-name"
    for {
      apiToken <- apiToken.toRight(apiTokenErrorMsg)
      username <- getValueOrEnvironmentVar(baseConfig.username, "CODACY_USERNAME").toRight(emptyUsernameMsg)
      projectName <- getValueOrEnvironmentVar(baseConfig.projectName, "CODACY_PROJECT_NAME").toRight(emptyProjectMsg)
      baseConf = BaseConfigWithApiToken(
        apiToken,
        username,
        projectName,
        baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl(sys.env)),
        baseConfig.commitUUID.map(CommitUUID),
        baseConfig.debugValue
      )
      validatedConfig <- validateConfig(baseConf) {
        case config: BaseConfigWithApiToken if config.apiToken.trim.isEmpty => Left(apiTokenErrorMsg)
        case config: BaseConfigWithApiToken if config.username.trim.isEmpty => Left(emptyUsernameMsg)
        case config: BaseConfigWithApiToken if config.projectName.trim.isEmpty => Left(emptyProjectMsg)
        case config => Right(config)
      }
    } yield {
      logger.info(s"Using API token API -> API base URL: ${validatedConfig.codacyApiBaseUrl}")
      validatedConfig
    }
  }

  private def getValueOrEnvironmentVar(value: Option[String], envVarName: String) =
    value.orElse(sys.env.get(envVarName))

  private def validateConfig(baseConf: BaseConfig)(additionalValidations: BaseConfig => Either[String, BaseConfig]) = {
    baseConf match {
      case config if !validUrl(config.codacyApiBaseUrl) =>
        val error = s"Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}"

        val help = if (!config.codacyApiBaseUrl.startsWith("http")) {
          "Maybe you forgot the http:// or https:// ?"
        }
        Left(s"$error\n$help")

      case config =>
        additionalValidations(config)
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
      case Some(value) if value.nonEmpty =>
        Right(value)
      case None =>
        Right(List.empty[File])
    }
  }
}
