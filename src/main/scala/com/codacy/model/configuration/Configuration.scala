package com.codacy.model.configuration

import java.io.File

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
    prefix: String
) extends Configuration {

  lazy val language: Option[Language] = languageOpt.flatMap(Languages.fromName)
}

case class FinalConfig(baseConfig: BaseConfig) extends Configuration

sealed abstract class BaseConfig(
    val projectTokenOpt: Option[String],
    val apiTokenOpt: Option[String],
    val codacyApiBaseUrl: String,
    val commitUUID: Option[CommitUUID],
    val debug: Boolean
)

case class BaseConfigWithProjectToken(
    projectToken: String,
    override val codacyApiBaseUrl: String,
    override val commitUUID: Option[CommitUUID],
    override val debug: Boolean
) extends BaseConfig(Some(projectToken), None, codacyApiBaseUrl, commitUUID, debug)

case class BaseConfigWithApiToken(
    apiToken: String,
    username: String,
    projectName: String,
    override val codacyApiBaseUrl: String,
    override val commitUUID: Option[CommitUUID],
    override val debug: Boolean
) extends BaseConfig(None, Some(apiToken), codacyApiBaseUrl, commitUUID, debug)

case class CommitUUID(value: String) extends AnyVal
