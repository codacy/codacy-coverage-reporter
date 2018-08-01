package com.codacy.rules

import java.net.URL

import cats.implicits._
import com.codacy.configuration.parser.{BaseCommandConfig, CommandConfiguration, Final, Report}
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{BaseConfig, Configuration, FinalConfig, ReportConfig}

import scala.language.implicitConversions
import scala.util.Try

class ConfigurationRules(cmdConfig: CommandConfiguration) {
  private val publicApiBaseUrl = "https://api.codacy.com"

  private val logger = LoggerHelper.logger(getClass, cmdConfig)


  lazy val validatedConfig: Configuration = {
    val config = validateConfig(cmdConfig)
    config.fold({ error =>
      logger.error(s"Invalid configuration: $error")
      sys.exit(1)
    },
      identity
    )
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
        case config if !config.hasKnownLanguage && !config.forceLanguage =>
          Left(s"Invalid language ${config.languageStr}")

        case _ =>
          Right(reportConf)
      }
    }

    for {
      baseConfig <- validateBaseConfig(reportConfig.baseConfig)
      reportConf = ReportConfig(
        baseConfig,
        reportConfig.language,
        reportConfig.forceLanguage,
        reportConfig.coverageReport,
        reportConfig.partial,
        reportConfig.prefix.getOrElse("")
      )
      validatedConfig <- validate(reportConf)
    } yield validatedConfig


  }

  private def validateBaseConfig(baseConfig: BaseCommandConfig): Either[String, BaseConfig] = {
    for {
      projectToken <- baseConfig.projectToken.fold(getProjectToken)(_.asRight)
      baseConf = BaseConfig(
        projectToken,
        baseConfig.codacyApiBaseUrl.getOrElse(getApiBaseUrl),
        baseConfig.commitUUID.orElse(commitUUIDOpt),
        baseConfig.debug
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

  private implicit def flagToBooleanConversion(flag: Option[Unit]): Boolean = flag.fold(false)(_ => true)

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

  private def getProjectToken: Either[String, String] = {
    Either.fromOption(
      sys.env.get("CODACY_PROJECT_TOKEN"),
      "Project token not provided and not available in environment variable \"CODACY_PROJECT_TOKEN\""
    )
  }

  private def validUrl(baseUrl: String) = {
    Try(new URL(baseUrl)).toOption.isDefined
  }

}
