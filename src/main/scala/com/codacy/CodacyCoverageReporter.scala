package com.codacy

import java.net.URL

import ch.qos.logback.classic.{Level, Logger}
import com.codacy.rules.{ConfigurationRules, ReportRules}
import org.slf4j.LoggerFactory

import scala.util.Try

object CodacyCoverageReporter {

  private val logger = {
    val logger = LoggerFactory.getLogger("com.codacy.CodacyCoverageReporter").asInstanceOf[Logger]
    logger
  }

  private[codacy] lazy val reportRules = new ReportRules(logger)
  private[codacy] lazy val configRules = new ConfigurationRules(logger)


  def main(args: Array[String]): Unit = {

    configRules.parseConfig(args) match {
      case Some(config) if !validUrl(config.codacyApiBaseUrl) =>
        logger.error(s"Error: Invalid CODACY_API_BASE_URL: ${config.codacyApiBaseUrl}")
        if (!config.codacyApiBaseUrl.startsWith("http")) {
          logger.error("Maybe you forgot the http:// or https:// ?")
        }

      case Some(config) if !config.hasKnownLanguage && !config.forceLanguage =>
        logger.error(s"Invalid language ${config.languageStr}")

      case Some(config) if config.projectToken.trim.nonEmpty =>
        if (config.debug) {
          logger.setLevel(Level.DEBUG)
        }
        logger.debug(config.toString)

        reportRules.codacyCoverage(config)
      case Some(config) if config.projectToken.trim.isEmpty =>
        logger.error("Error: Missing option --projectToken")
      case _ =>
    }

  }

  private def validUrl(baseUrl: String) = {
    Try(new URL(baseUrl)).toOption.isDefined
  }

}
