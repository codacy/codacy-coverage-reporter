package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{FinalConfig, ReportConfig}
import com.typesafe.scalalogging.StrictLogging
import com.codacy.rules.ConfigurationRules

object CodacyCoverageReporter extends ConfigurationParsingApp with StrictLogging {
  private val Success = 0
  private val Failure = 1

  def run(commandConfig: CommandConfiguration): Int = {
    if (commandConfig.baseConfig.skipValue) {
      logger.info("Skip reporting coverage")
      Success
    } else {
      val result: Either[String, String] = sendReport(commandConfig)
      result.fold({ error =>
        logger.error(error)
        Failure
      }, { successMessage =>
        logger.info(successMessage)
        Success
      })

    }
  }

  private def sendReport(commandConfig: CommandConfiguration) = {
    val configRules = new ConfigurationRules(commandConfig)

    configRules.validatedConfig.flatMap { validatedConfig =>
      val components = new Components(validatedConfig)
      LoggerHelper.setLoggerLevel(logger, validatedConfig.baseConfig.debug)

      logger.debug(validatedConfig.toString)

      validatedConfig match {
        case config: ReportConfig =>
          components.reportRules.codacyCoverage(config)

        case config: FinalConfig =>
          components.reportRules.finalReport(config)
      }
    }
  }
}
