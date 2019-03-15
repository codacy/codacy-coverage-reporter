package com.codacy.model.configuration

import java.io.File

import com.codacy.plugins.api.languages.{Language, Languages}

sealed trait Configuration {
  def baseConfig: BaseConfig
}

case class ReportConfig(
    baseConfig: BaseConfig,
    languageStr: String,
    forceLanguage: Boolean,
    coverageReport: File,
    partial: Boolean,
    prefix: String
) extends Configuration {

  lazy val language: Option[Language] = Languages.fromName(languageStr)
}

case class FinalConfig(baseConfig: BaseConfig) extends Configuration

case class BaseConfig(projectToken: String, codacyApiBaseUrl: String, commitUUID: Option[String], debug: Boolean)
