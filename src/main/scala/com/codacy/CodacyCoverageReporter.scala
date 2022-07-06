package com.codacy

import com.codacy.configuration.parser.{CommandConfiguration, ConfigurationParsingApp}
import com.codacy.di.Components
import com.codacy.model.configuration.{FinalConfig, ReportConfig}
import com.codacy.rules.ConfigurationRules
import wvlet.airframe.log
import wvlet.log.{LogSupport, Logger}

object CodacyCoverageReporter extends ConfigurationParsingApp with LogSupport {
  log.initNoColor

  def run(commandConfig: CommandConfiguration): Int = {
    val noAvailableTokens = commandConfig.baseConfig.projectToken.isEmpty && commandConfig.baseConfig.apiToken.isEmpty
    if (commandConfig.baseConfig.skipValue && noAvailableTokens) {
      logger.info("Skip reporting coverage")
      0
    } else {
      val sleepTime = 10 // seconds
      val retryTimes = 3 // total number of sending coverage attempts
      retryReport(sleepTime, currentRetry = 1, retryTimes)(commandConfig, sys.env)
      1
    }
  }

  private def retryReport(sleepTime: Int, currentRetry: Int, retryTimes: Int)(
      commandConfig: CommandConfiguration,
      envVars: Map[String, String]
  ): AnyVal = {
    if (currentRetry <= retryTimes) {
      if (currentRetry > 1) {
        logger.info("Sleeping " + sleepTime + " seconds...")
        Thread.sleep(sleepTime * 1000)
      }
      val result: Either[String, String] = sendReport(commandConfig, sys.env)
      result match {
        case Left(message) =>
          logger.error("Attempt no." + currentRetry + " of sending coverage failed: " + message)
          retryReport(sleepTime, currentRetry + 1, retryTimes)(commandConfig, sys.env)
          1
        case Right(message) =>
          logger.info(message)
          0
      }
    } else {
      logger.info("Attempting to send coverage failed.")
      1
    }

  }

  private def sendReport(commandConfig: CommandConfiguration, envVars: Map[String, String]) = {
    val configRules = new ConfigurationRules(commandConfig, envVars)

    configRules.validatedConfig.flatMap { validatedConfig =>
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
