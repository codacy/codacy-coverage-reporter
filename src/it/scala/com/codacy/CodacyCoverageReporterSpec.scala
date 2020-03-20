package com.codacy

import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.ReportConfig
import com.codacy.rules.ConfigurationRules
import org.scalatest.{EitherValues, Matchers, WordSpec}

class CodacyCoverageReporterSpec extends WordSpec with Matchers with EitherValues {
  private val apiToken = sys.env.get("TEST_CODACY_API_TOKEN")
  private val projectToken = sys.env.get("TEST_CODACY_PROJECT_TOKEN")
  private val commitUuid = sys.env.get("TEST_COMMIT_UUID")
  private val projectName = sys.env.get("TEST_PROJECT_NAME")
  private val username = sys.env.get("TEST_USERNAME")
  private val apiBaseUrl = sys.env.get("TEST_BASE_API_URL")

  private def runCoverageReport(
      projectToken: Option[String],
      apiToken: Option[String],
      username: Option[String],
      projectName: Option[String],
      commitUuid: Option[String]
  ) = {
    val baseConfig =
      BaseCommandConfig(projectToken, apiToken, username, projectName, apiBaseUrl, commitUuid)

    val commandConfig = Report(
      baseConfig = baseConfig,
      language = None,
      coverageReports = Some(List(new File("src/test/resources/dotcover-example.xml"))),
      prefix = None
    )

    val configRules = new ConfigurationRules(commandConfig, Map())
    val components = new Components(configRules.validatedConfig.right.value)

    configRules.validatedConfig.right.value match {
      case config: ReportConfig => components.reportRules.codacyCoverage(config)
      case _ => fail("Config should be of type ReportConfig")
    }
  }

  "run" should {
    "be successful" when {
      "using a project token to send coverage" in {
        val result = runCoverageReport(projectToken, None, None, None, commitUuid)

        result shouldBe 'right
      }

      "using an API token to send coverage" in {
        // empty projectToken so we skip project token
        // passing None will pick the token used for the codacy-coverage-reporter project
        val result = runCoverageReport(None, apiToken, username, projectName, commitUuid)

        result shouldBe 'right
      }
    }

    "fail" when {
      "project token is invalid" in {
        val result = runCoverageReport(Some("invalid token"), None, None, None, commitUuid)

        result shouldBe 'left
      }

      "API token is invalid" in {
        val result = runCoverageReport(None, Some("invalid token"), username, projectName, commitUuid)

        result shouldBe 'left
      }
    }
  }
}
