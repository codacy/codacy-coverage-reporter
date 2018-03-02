package com.codacy

import ch.qos.logback.classic.{Level, Logger}
import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.model.configuration.{Configuration, FinalConfig, ReportConfig}
import com.codacy.rules.{ConfigurationRules, ReportRules}
import org.slf4j.LoggerFactory

object CodacyCoverageReporter extends ConfigurationParsingApp {

  private val logger = {
    val logger = LoggerFactory.getLogger("com.codacy.CodacyCoverageReporter").asInstanceOf[Logger]
    logger
  }

  private[codacy] lazy val reportRules = new ReportRules(logger)
  private[codacy] lazy val configRules = new ConfigurationRules(logger)


  def run(commandConfig: CommandConfiguration): Unit = {
    val validatedConfig = configRules.validateConfig(commandConfig)

    validatedConfig.foreach { config =>
      setLoggerLevel(config)
      logger.debug(commandConfig.toString)
    }

    validatedConfig
      .fold({ error =>
        logger.error(s"Invalid configuration: \n$error")
        sys.exit(1)
      }, {
        case config: ReportConfig =>
          reportRules.codacyCoverage(config)

        case config: FinalConfig =>
          reportRules.finalReport(config)
      })
  }


  private def setLoggerLevel(config: Configuration): Unit = {
    if (config.baseConfig.debug) {
      logger.setLevel(Level.DEBUG)
    }
  }

}
