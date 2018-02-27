package com.codacy.rules

import java.io.File

import ch.qos.logback.classic.Logger
import com.codacy.api.Language
import com.codacy.model.configuration.Config

class ConfigurationRules(logger: => Logger) {

  val emptyConfig =
    Config(
      Language.Python.toString,
      forceLanguage = false,
      ConfigurationRules.getProjectToken,
      new File("coverage.xml"),
      ConfigurationRules.getApiBaseUrl,
      "",
      ConfigurationRules.commitUUIDOpt,
      debug = false)

}


object ConfigurationRules {

  private val publicApiBaseUrl = "https://api.codacy.com"

  private def getApiBaseUrl: String = {
    sys.env.getOrElse("CODACY_API_BASE_URL", publicApiBaseUrl)
  }

  private def getProjectToken: String = {
    sys.env.getOrElse("CODACY_PROJECT_TOKEN", "")
  }

  private lazy val commitUUIDOpt: Option[String] = {
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

}
