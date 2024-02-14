package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.model.configuration.{Configuration, FinalConfig, ReportConfig}
import com.codacy.rules.ConfigurationRules
import wvlet.airframe.log
import wvlet.log.{LogSupport, Logger}

object CodacyCoverageReporter extends ConfigurationParsingApp with LogSupport {
  log.initNoColor

  def run(commandConfig: CommandConfiguration): Int = {
    val configRules = new ConfigurationRules(commandConfig, sys.env)

    val noAvailableTokens =
      configRules.getProjectToken(commandConfig.baseConfig).isEmpty &&
        configRules.getApiToken(commandConfig.baseConfig).isEmpty
    if (commandConfig.baseConfig.skipValue && noAvailableTokens) {
      logger.info("Skip reporting coverage")
      0
    } else {
      sendReport(configRules.validatedConfig) match {
        case Right(message) =>
          logger.info(message)
          0
        case Left(message) =>
          logger.error(message)
          1
      }
    }
  }

  private def sendReport(validatedConfig: Either[String, Configuration]) = {
    validatedConfig.flatMap { validatedConfig =>
      val components = new Components(validatedConfig)

      if (validatedConfig.baseConfig.debug) {
        Logger("com.codacy").setLogLevel(wvlet.log.LogLevel.DEBUG)
      }

      validatedConfig match {
        case config: ReportConfig =>
          components.reportRules.codacyCoverage(config)

        case config: FinalConfig =>
          components.reportRules.finalReport(config)
      }
    }
  }
}
