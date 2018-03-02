package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.{FinalConfig, ReportConfig}

object CodacyCoverageReporter extends ConfigurationParsingApp {

  def run(commandConfig: CommandConfiguration): Unit = {
    val components = new Components(commandConfig)
    import components._

    val logger = LoggerHelper.logger(this.getClass, validatedConfig)

    logger.debug(validatedConfig.toString)

    validatedConfig match {
      case config: ReportConfig =>
        reportRules.codacyCoverage(config)

      case config: FinalConfig =>
        reportRules.finalReport(config)
    }
  }

}
