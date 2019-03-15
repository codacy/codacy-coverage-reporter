package com.codacy.di

import com.codacy.api.client.CodacyClient
import com.codacy.api.service.CoverageServices
import com.codacy.configuration.parser.CommandConfiguration
import com.codacy.helpers.LoggerHelper
import com.codacy.model.configuration.Configuration
import com.codacy.rules.{ConfigurationRules, ReportRules}
import rapture.json.jsonBackends.play._

class Components(private val cmdConfig: CommandConfiguration) {
  lazy val validatedConfig: Configuration = configRules.validatedConfig

  lazy val configRules = new ConfigurationRules(cmdConfig)
  lazy val reportRules = new ReportRules(validatedConfig, coverageServices)

  lazy val codacyClient = new CodacyClient(
    Some(validatedConfig.baseConfig.codacyApiBaseUrl),
    projectToken = Some(validatedConfig.baseConfig.projectToken)
  )
  lazy val coverageServices = new CoverageServices(codacyClient)

}
