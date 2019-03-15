package com.codacy.model.configuration

import java.io.File

import com.codacy.api.Language

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

  lazy val language: Language.Value =
    Language.values.find(_.toString == languageStr).getOrElse(Language.NotDefined)

  lazy val hasKnownLanguage: Boolean = language != Language.NotDefined
}

case class FinalConfig(baseConfig: BaseConfig) extends Configuration

case class BaseConfig(projectToken: String, codacyApiBaseUrl: String, commitUUID: Option[String], debug: Boolean)
