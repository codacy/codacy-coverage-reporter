package com.codacy.helpers

import ch.qos.logback.classic.Level
import com.codacy.configuration.parser.CommandConfiguration
import com.codacy.model.configuration.Configuration
import org.log4s.Logger

object LoggerHelper {

  def logger(clazz: Class[_], config: CommandConfiguration): Logger =
    getLogger(clazz, config.baseConfig.debug.fold(false)(_ => true))

  def logger(clazz: Class[_], config: Configuration): Logger =
    getLogger(clazz, config.baseConfig.debug)

  private def getLogger(clazz: Class[_], debug: Boolean): org.log4s.Logger = {
    val logger = org.log4s.getLogger(clazz)
    setLoggerLevel(logger, debug)
    logger
  }

  private def setLoggerLevel(logger: org.log4s.Logger, debug: Boolean): Unit = {
    if (debug) {
      logger.logger
        .asInstanceOf[ch.qos.logback.classic.Logger]
        .setLevel(Level.DEBUG)
    }
  }
}
