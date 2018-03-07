package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{FinalConfig, ReportConfig}

object CodacyCoverageReporter extends ConfigurationParsingApp {

  def run(commandConfig: CommandConfiguration): Unit = {
    val components = new Components(commandConfig)

    val validatedConfig = components.validatedConfig

    val logger = LoggerHelper.logger(getClass, validatedConfig)

    logger.debug(validatedConfig.toString)

    val result = validatedConfig match {
      case config: ReportConfig =>
        components.reportRules.codacyCoverage(config)

      case config: FinalConfig =>
        components.reportRules.finalReport(config)
    }

    result.fold({ error =>
      logger.error(error)
      sys.exit(1)
    }, { successMessage =>
      logger.info(successMessage)
      sys.exit(0)
    })
  }

}
