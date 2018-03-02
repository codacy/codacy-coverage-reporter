package com.codacy

import cats.implicits._
import ch.qos.logback.classic.Level
import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.model.configuration.{Configuration, FinalConfig, ReportConfig}
import com.codacy.rules.{ConfigurationRules, ReportRules}
import org.log4s.getLogger

object CodacyCoverageReporter extends ConfigurationParsingApp {

  private val logger = getLogger

  private[codacy] lazy val reportRules = new ReportRules
  private[codacy] lazy val configRules = new ConfigurationRules


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
      logger.logger
        .asInstanceOf[ch.qos.logback.classic.Logger]
        .setLevel(Level.DEBUG)
    }
  }

}
