package com.codacy

import java.io.File
import java.net.URL

import ch.qos.logback.classic.{Level, Logger}
import com.codacy.model.configuration.Config
import com.codacy.rules.{ConfigurationRules, ReportRules}
import org.slf4j.LoggerFactory
import scopt.OptionParser

import scala.util.Try

object CodacyCoverageReporter {

  private val logger = {
    val logger = LoggerFactory.getLogger("com.codacy.CodacyCoverageReporter").asInstanceOf[Logger]
    logger
  }

  private[codacy] lazy val reportRules = new ReportRules(logger)
  private[codacy] lazy val configRules = new ConfigurationRules(logger)


  private def validUrl(baseUrl: String) = {
    Try(new URL(baseUrl)).toOption.isDefined
  }

  def main(args: Array[String]): Unit = {

    val parser = buildParser

    parser.parse(args, configRules.emptyConfig) match {
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

  def buildParser: OptionParser[Config] = {
    new scopt.OptionParser[Config]("codacy-coverage-reporter") {
      head("codacy-coverage-reporter", getClass.getPackage.getImplementationVersion)
      opt[String]('l', "language").required().action { (x, c) =>
        c.copy(languageStr = x)
      }.text("your project language")
      opt[Unit]('f', "forceLanguage").optional().hidden().action { (_, c) =>
        c.copy(forceLanguage = true)
      }
      opt[String]('t', "projectToken").optional().action { (x, c) =>
        c.copy(projectToken = x)
      }.text("your project API token")
      opt[File]('r', "coverageReport").required().action { (x, c) =>
        c.copy(coverageReport = x)
      }.text("your project coverage file name")
      opt[String]("codacyApiBaseUrl").optional().action { (x, c) =>
        c.copy(codacyApiBaseUrl = x)
      }.text("the base URL for the Codacy API")
      opt[String]("prefix").optional().action { (x, c) =>
        c.copy(prefix = x)
      }.text("the project path prefix")
      opt[String]("commitUUID").optional().action { (x, c) =>
        c.copy(commitUUID = Some(x))
      }.text("your commitUUID")
      opt[Unit]("debug").optional().hidden().action { (_, c) =>
        c.copy(debug = true)
      }
      help("help").text("prints this usage text")
    }
  }

}
