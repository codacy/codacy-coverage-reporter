package com.codacy.model.configuration

import java.io.File

import com.codacy.parsers.CoverageParser
import com.codacy.plugins.api.languages.{Language, Languages}

sealed trait Configuration {
  def baseConfig: BaseConfig
}

case class ReportConfig(
    baseConfig: BaseConfig,
    languageOpt: Option[String],
    forceLanguage: Boolean,
    coverageReports: List[File],
    partial: Boolean,
    prefix: String,
    forceCoverageParser: Option[CoverageParser]
) extends Configuration {

  lazy val language: Option[Language] = languageOpt.flatMap(Languages.fromName)
}

case class FinalConfig(baseConfig: BaseConfig) extends Configuration

sealed trait AuthenticationConfig

case class ProjectTokenAuthenticationConfig(projectToken: String) extends AuthenticationConfig

case class ApiTokenAuthenticationConfig(apiToken: String, username: String, projectName: String)
    extends AuthenticationConfig

case class BaseConfig(
    authentication: AuthenticationConfig,
    codacyApiBaseUrl: String,
    commitUUID: Option[CommitUUID],
    debug: Boolean
)

case class CommitUUID(value: String) extends AnyVal
