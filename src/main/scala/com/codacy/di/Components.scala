package com.codacy.di

import com.codacy.api.client.CodacyClient
import com.codacy.api.service.CoverageServices
import com.codacy.configuration.parser.CommandConfiguration
import com.codacy.model.configuration.{ApiTokenAuthenticationConfig, Configuration, ProjectTokenAuthenticationConfig}
import com.codacy.rules.{ConfigurationRules, ReportRules}

class Components(private val cmdConfig: CommandConfiguration) {
  lazy val validatedConfig: Configuration = configRules.validatedConfig

  lazy val configRules = new ConfigurationRules(cmdConfig)
  lazy val reportRules = new ReportRules(coverageServices)

  lazy private val (projectToken, apiToken) = validatedConfig.baseConfig.authentication match {
    case ProjectTokenAuthenticationConfig(projectToken) =>
      (Some(projectToken), None)
    case ApiTokenAuthenticationConfig(apiToken, _, _) =>
      (None, Some(apiToken))
  }

  lazy val codacyClient = new CodacyClient(Some(validatedConfig.baseConfig.codacyApiBaseUrl), apiToken, projectToken)

  lazy val coverageServices = new CoverageServices(codacyClient)

}
