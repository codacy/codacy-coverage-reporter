package com.codacy.rules

import java.io.File

import ch.qos.logback.classic.Logger
import com.codacy.CodacyCoverageReporter.configRules
import com.codacy.api.Language
import com.codacy.model.configuration.Config
import scopt.OptionParser

class ConfigurationRules(logger: => Logger) {
  private val publicApiBaseUrl = "https://api.codacy.com"

  val emptyConfig =
    Config(
      Language.Python.toString,
      forceLanguage = false,
      getProjectToken,
      new File("coverage.xml"),
      getApiBaseUrl,
      "",
      commitUUIDOpt,
      debug = false)


  def parseConfig(args: Array[String]): Option[Config] =
    buildParser.parse(args, configRules.emptyConfig)

  private def buildParser: OptionParser[Config] = {
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

  private def commitUUIDOpt: Option[String] = {
    getNonEmptyEnv("CI_COMMIT") orElse
      getNonEmptyEnv("TRAVIS_PULL_REQUEST_SHA") orElse
      getNonEmptyEnv("TRAVIS_COMMIT") orElse
      getNonEmptyEnv("DRONE_COMMIT") orElse
      getNonEmptyEnv("CIRCLE_SHA1") orElse
      getNonEmptyEnv("CI_COMMIT_ID") orElse
      getNonEmptyEnv("WERCKER_GIT_COMMIT") orElse
      getNonEmptyEnv("CODEBUILD_RESOLVED_SOURCE_VERSION")
        .filter(_.trim.nonEmpty)
  }

  private def getNonEmptyEnv(key: String): Option[String] = {
    sys.env.get(key).filter(_.trim.nonEmpty)
  }

  private def getApiBaseUrl: String = {
    sys.env.getOrElse("CODACY_API_BASE_URL", publicApiBaseUrl)
  }

  private def getProjectToken: String = {
    sys.env.getOrElse("CODACY_PROJECT_TOKEN", "")
  }

}
