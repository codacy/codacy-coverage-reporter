package com.codacy.helpers

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.Logger

object LoggerHelper {

  def setLoggerLevel(logger: Logger, debug: Boolean): Unit = {
    if (debug) {
      logger.underlying
        .asInstanceOf[ch.qos.logback.classic.Logger]
        .setLevel(Level.DEBUG)
    }
  }

}
