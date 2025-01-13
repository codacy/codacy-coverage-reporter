package com.codacy.model.configuration

import java.io.File
import scala.util.matching.Regex
import com.codacy.api.OrganizationProvider
import com.codacy.api.client.RequestTimeout
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

case class ApiTokenAuthenticationConfig(
    apiToken: String,
    organizationProvider: OrganizationProvider.Value,
    username: String,
    projectName: String
) extends AuthenticationConfig

case class BaseConfig(
    authentication: AuthenticationConfig,
    codacyApiBaseUrl: String,
    commitUUID: Option[CommitUUID],
    debug: Boolean,
    timeout: RequestTimeout,
    sleepTime: Int,
    numRetries: Int,
    skipSslVerification: Boolean
)

sealed trait CommitUUID extends Any {
  def value: String
}

object CommitUUID {
  /* The number of characters in a commit UUID. */
  val length: Int = 40

  /* Regex to help detect a commit UUID. */
  val regex: Regex = s"[0-9a-fA-F]{$length}".r

  private def isValid(commitUUID: String): Boolean =
    commitUUID.length == length && regex.findFirstIn(commitUUID).isDefined

  /** Either creates a [[CommitUUID]], if `commitUUID` is valid, or returns an error string, to display to the user. */
  def fromString(commitUUID: String): Either[String, CommitUUID] =
    if (isValid(commitUUID)) {
      Right(CommitUUIDImpl(commitUUID))
    } else {
      Left("Commit SHA-1 hash isn't valid. Make sure it consists of 40 hexadecimal characters.")
    }

  /** Commit UUID class that guarantees it contains a valid commit SHA, since it can only be instantiated via
    * [[fromString()]] */
  private[CommitUUID] case class CommitUUIDImpl(value: String) extends AnyVal with CommitUUID
}
