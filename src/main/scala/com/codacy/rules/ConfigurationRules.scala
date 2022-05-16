package com.codacy.rules

import com.codacy.api.OrganizationProvider
import com.codacy.api.client.RequestTimeout

import java.io.File
import java.net.URL
import scala.util.Try
import wvlet.log.LogSupport
import com.codacy.configuration.parser.{BaseCommandConfig, CommandConfiguration, Final, Report}
import com.codacy.model.configuration._

class ConfigurationRules(cmdConfig: CommandConfiguration, envVars: Map[String, String]) extends LogSupport {
  private[rules] val publicApiBaseUrl = "https://api.codacy.com"

  lazy val validatedConfig: Either[String, Configuration] = {
    val config = validateConfig(cmdConfig)
    config.left.map(error => s"Invalid configuration: $error")
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
        reportConfig.prefix.getOrElse(""),
        reportConfig.forceCoverageParser
      )
      validatedConfig <- validate(reportConf)
    } yield validatedConfig

  }

  private[rules] def validateBaseConfig(baseConfig: BaseCommandConfig): Either[String, BaseConfig] = {
    for {
      authConfig <- validateAuthConfig(baseConfig)
      commitUUID <- validateCommitUUIDConfig(baseConfig)
      baseConf = BaseConfig(
        authConfig,
        baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl),
        commitUUID,
        baseConfig.debugValue,
        timeout = RequestTimeout(connTimeoutMs = 1000, readTimeoutMs = baseConfig.httpTimeout)
      )
      validatedConfig <- validateBaseConfigUrl(baseConf)
    } yield {
      logger.info(s"API base URL: ${validatedConfig.codacyApiBaseUrl}")
      validatedConfig
    }
  }

  private def validateAuthConfig(baseCommandConfig: BaseCommandConfig): Either[String, AuthenticationConfig] = {
    val errorMessage =
      "Either a project or account API token must be provided or available in an environment variable"

    val projectToken = getValueOrEnvironmentVar(baseCommandConfig.projectToken, "CODACY_PROJECT_TOKEN")
    val apiToken = getValueOrEnvironmentVar(baseCommandConfig.apiToken, "CODACY_API_TOKEN")

    if (projectToken.isDefined)
      validateProjectTokenAuth(projectToken)
    else if (apiToken.isDefined)
      validateApiTokenAuth(baseCommandConfig, apiToken)
    else
      Left(errorMessage)
  }

  private def validateCommitUUIDConfig(baseCommandConfig: BaseCommandConfig): Either[String, Option[CommitUUID]] = {
    val emptyCommitUUID: Either[String, Option[CommitUUID]] = Right(None)

    baseCommandConfig.commitUUID.fold(emptyCommitUUID) { commitUUIDStr =>
      CommitUUID.fromString(commitUUIDStr).map(Option(_))
    }
  }

  private def validateProjectTokenAuth(projectToken: Option[String]) =
    projectToken.filter(_.nonEmpty) match {
      case None => Left("Empty argument for --project-token")
      case Some(projectToken) => Right(ProjectTokenAuthenticationConfig(projectToken))
    }

  private def validateApiTokenAuth(baseCommandConfig: BaseCommandConfig, apiToken: Option[String]) = {
    for {
      apiToken <- apiToken.filter(_.nonEmpty).toRight("Empty argument --api-token")
      organizationProvider <- baseCommandConfig.organizationProvider
        .orElse(
          envVars
            .get("CODACY_ORGANIZATION_PROVIDER")
            .flatMap(provider => OrganizationProvider.values.find(_.toString.equalsIgnoreCase(provider)))
        )
        .toRight("Empty argument --organization-provider")
      username <- getValueOrEnvironmentVar(baseCommandConfig.username, "CODACY_USERNAME")
        .filter(_.nonEmpty)
        .toRight("Empty argument --username")
      projectName <- getValueOrEnvironmentVar(baseCommandConfig.projectName, "CODACY_PROJECT_NAME")
        .filter(_.nonEmpty)
        .toRight("Empty argument --project-name")
    } yield ApiTokenAuthenticationConfig(apiToken, organizationProvider, username, projectName)
  }

  private def getValueOrEnvironmentVar(value: Option[String], envVarName: String) =
    value.orElse(envVars.get(envVarName))

  private def validateBaseConfigUrl(baseConfig: BaseConfig) = baseConfig match {
    case config if !validUrl(config.codacyApiBaseUrl) =>
      val error = s"Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}"

      val help = if (!config.codacyApiBaseUrl.startsWith("http")) {
        "Maybe you forgot the http:// or https:// ?"
      }
      Left(s"""|$error
               |$help""".stripMargin)

    case config => Right(config)
  }

  /**
    * Get API base URL
    *
    * This function try to get the API base URL from environment variables, and if not
    * found, fallback to the public API base URL
    *
    * @return api base url
    */
  private[rules] def getApiBaseUrl: String = {
    envVars.getOrElse("CODACY_API_BASE_URL", publicApiBaseUrl)
  }

  /**
    * Validate an URL
    *
    * This function check if the url is valid or not
    *
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
    *
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
