package com.codacy.model.configuration

import java.io.File

import com.codacy.api.Language

case class Config(languageStr: String,
                      forceLanguage: Boolean,
                      projectToken: String,
                      coverageReport: File,
                      codacyApiBaseUrl: String,
                      prefix: String,
                      commitUUID: Option[String],
                      debug: Boolean
                     ) {

  lazy val language: Language.Value =
    Language.values.find(_.toString == languageStr).getOrElse(Language.NotDefined)

  lazy val hasKnownLanguage: Boolean = language != Language.NotDefined
}
