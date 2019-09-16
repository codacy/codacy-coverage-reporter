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

case class BaseConfig(projectToken: String, codacyApiBaseUrl: String, commitUUID: Option[CommitUUID], debug: Boolean)

case class CommitUUID(value: String) extends AnyVal
