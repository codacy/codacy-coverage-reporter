package com.codacy.model.configuration

import java.io.File

import com.codacy.model.configuration.CommitUUID.regex
import com.codacy.parsers.CoverageParser
import com.codacy.plugins.api.languages.{Language, Languages}

import scala.util.matching.Regex

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

case class CommitUUID(value: String) extends AnyVal {

  def isValid: Boolean = value.length == CommitUUID.length && regex.findFirstIn(value).isDefined
}

object CommitUUID {
  /* The number of characters in a commit UUID. */
  val length: Int = 40

  /* Regex to help detect a commit UUID. */
  val regex: Regex = s"[0-9a-fA-F]{$length}".r
}
