package com.codacy.rules

import java.net.URL

import cats.implicits._
import com.codacy.configuration.parser.{BaseCommandConfig, CommandConfiguration, Final, Report}
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{BaseConfig, Configuration, FinalConfig, ReportConfig}

import scala.util.Try

class ConfigurationRules(cmdConfig: CommandConfiguration) {
  private val publicApiBaseUrl = "https://api.codacy.com"

  private val logger = LoggerHelper.logger(getClass, cmdConfig)


  lazy val validatedConfig: Configuration = {
    validateConfig(cmdConfig)
      .fold({ error =>
        logger.error(s"Invalid configuration: \n$error")
        sys.exit(1)
      },
        identity
      )
  }

  private def validateConfig(cmdConfig: CommandConfiguration): Either[String, Configuration] = {
    cmdConfig match {
      case config: Report =>
        validateReportConfig(config)
      case config: Final =>
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
        case config if !config.hasKnownLanguage && !config.forceLanguage =>
          logger.error(s"Error: Invalid language ${config.languageStr}")
          Left("")

        case _ =>
          Right(reportConf)
      }
    }

    for {
      baseConfig <- validateBaseConfig(reportConfig.baseConfig)
      reportConf = ReportConfig(
        baseConfig,
        reportConfig.language,
        reportConfig.forceLanguage.fold(false)(_ => true),
        reportConfig.coverageReport,
        reportConfig.prefix.getOrElse("")
      )
      validatedConfig <- validate(reportConf)
    } yield validatedConfig


  }

  private def validateBaseConfig(baseConfig: BaseCommandConfig): Either[String, BaseConfig] = {
    val baseConf = BaseConfig(
      baseConfig.projectToken.getOrElse(getProjectToken),
      baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl),
      baseConfig.commitUUID.orElse(commitUUIDOpt),
      baseConfig.debug.fold(false)(_ => true)
    )

    baseConf match {
      case config if !validUrl(config.codacyApiBaseUrl) =>
        logger.error(s"Error: Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}")
        if (!config.codacyApiBaseUrl.startsWith("http")) {
          logger.error("Maybe you forgot the http:// or https:// ?")
        }
        Left("")

      case config if config.projectToken.trim.isEmpty =>
        logger.error("Error: Missing option --project-token")
        Left("")

      case _ =>
        Right(baseConf)
    }
  }

  private def commitUUIDOpt: Option[String] = {
    getNonEmptyEnv("CI_COMMIT") orElse
      getNonEmptyEnv("TRAVIS_PULL_REQUEST_SHA") orElse
      getNonEmptyEnv("TRAVIS_COMMIT") orElse
      getNonEmptyEnv("DRONE_COMMIT") orElse
      getNonEmptyEnv("CIRCLE_SHA1") orElse
      getNonEmptyEnv("CI_COMMIT_ID") orElse
      getNonEmptyEnv("WERCKER_GIT_COMMIT") orElse
      getNonEmptyEnv("CODEBUILD_RESOLVED_SOURCE_VERSION")
        .filter(_.trim.nonEmpty)
  }

  private def getNonEmptyEnv(key: String): Option[String] = {
    sys.env.get(key).filter(_.trim.nonEmpty)
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

}
