package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{FinalConfig, ReportConfig}
import com.typesafe.scalalogging.StrictLogging
import com.codacy.rules.ConfigurationRules

object CodacyCoverageReporter extends ConfigurationParsingApp with StrictLogging {

  def run(commandConfig: CommandConfiguration): Int = {
    if (commandConfig.baseConfig.skipValue) {
      logger.info("Skip reporting coverage")
      0
    } else {
      val configRules = new ConfigurationRules(commandConfig)

      val result = configRules.validatedConfig.flatMap { validatedConfig =>
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
      result.fold({ error =>
        logger.error(error)
        1
      }, { successMessage =>
        logger.info(successMessage)
        0
      })

    }
  }

}
