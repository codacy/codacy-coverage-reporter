package com.codacy

import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import org.scalatest.{Matchers, WordSpec}

class CodacyCoverageReporterSpec extends WordSpec with Matchers {
  private val apiToken = sys.env.get("TEST_CODACY_API_TOKEN")
  private val projectToken = sys.env.get("TEST_CODACY_PROJECT_TOKEN")
  private val commitUuid = sys.env.get("TEST_COMMIT_UUID")
  private val projectName = sys.env.get("TEST_PROJECT_NAME")
  private val username = sys.env.get("TEST_USERNAME")
  private val apiBaseUrl = sys.env.get("TEST_BASE_API_URL")

  private def getConfig(
      projectToken: Option[String],
      apiToken: Option[String],
      username: Option[String],
      projectName: Option[String],
      commitUuid: Option[String]
  ) = {
    val baseConfig =
      BaseCommandConfig(projectToken, apiToken, username, projectName, apiBaseUrl, commitUuid)

    Report(
      baseConfig = baseConfig,
      language = None,
      coverageReports = Some(List(new File("src/test/resources/dotcover-example.xml"))),
      prefix = None
    )
  }

  "run" should {
    "be successful" when {
      "using a project token to send coverage" in {
        val config = getConfig(projectToken, None, None, None, commitUuid)
        val result = CodacyCoverageReporter.sendReport(config)

        result shouldBe 'right
      }

      "using an API token to send coverage" in {
        // empty projectToken so we skip project token
        // passing None will pick the token used for the codacy-coverage-reporter project
        val config = getConfig(Some(""), apiToken, username, projectName, commitUuid)
        val result = CodacyCoverageReporter.sendReport(config)

        result shouldBe 'right
      }
    }

    "fail" when {
      "project token is invalid" in {
        val config = getConfig(Some("invalid token"), None, None, None, commitUuid)
        val result = CodacyCoverageReporter.sendReport(config)

        result shouldBe 'left
      }

      "API token is invalid" in {
        val config = getConfig(Some(""), Some("invalid token"), username, projectName, commitUuid)
        val result = CodacyCoverageReporter.sendReport(config)

        result shouldBe 'left
      }
    }
  }
}
