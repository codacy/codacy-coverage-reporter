package com.codacy.helpers

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.Logger

object LoggerHelper {

  def setLoggerLevel(logger: Logger, debug: Boolean): Unit = {
    val level = if (debug) Level.DEBUG else Level.INFO
    logger.underlying match {
      case l: ch.qos.logback.classic.Logger => l.setLevel(level)
      case _ =>
    }
  }

}
