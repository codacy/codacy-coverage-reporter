package com.codacy.di

import com.codacy.api.client.CodacyClient
import com.codacy.api.service.CoverageServices
import com.codacy.model.configuration.{ApiTokenAuthenticationConfig, Configuration, ProjectTokenAuthenticationConfig}
import com.codacy.rules.ReportRules
import com.codacy.rules.file.GitFileFetcher

class Components(private val validatedConfig: Configuration) {
  lazy val reportRules = new ReportRules(coverageServices, new GitFileFetcher)

  lazy private val (projectToken, apiToken) = validatedConfig.baseConfig.authentication match {
    case ProjectTokenAuthenticationConfig(projectToken) =>
      (Some(projectToken), None)
    case ApiTokenAuthenticationConfig(apiToken, _, _, _) =>
      (None, Some(apiToken))
  }

  lazy val codacyClient = new CodacyClient(Some(validatedConfig.baseConfig.codacyApiBaseUrl), apiToken, projectToken)

  lazy val coverageServices = new CoverageServices(codacyClient)

}
