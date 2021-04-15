package com.codacy.rules

import com.codacy.api.OrganizationProvider
import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.ReportConfig
import com.codacy.plugins.api.languages.Languages
import org.scalatest.Inside._
import org.scalatest._

import java.io.File

class ConfigurationRulesSpec extends WordSpec with Matchers with OptionValues with EitherValues {

  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://example.com"

  val baseConf = BaseCommandConfig(Some(projToken), None, None, None, None, Some(apiBaseUrl), None)

  val conf =
    Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None, forceCoverageParser = None)

  val configRules = new ConfigurationRules(conf, Map.empty)
  val validatedConfig = configRules.validatedConfig.right.value

  val components = new Components(validatedConfig)

  "ConfigurationRules" should {
    "transform configuration" in {
      inside(validatedConfig) {
        case config: ReportConfig =>
          config.language.value should be(Languages.Scala)
          config.coverageReports.map(_.toString) should be(List("coverage.xml"))
          config.prefix should be("")
          config.forceLanguage should be(false)
          config.languageOpt should be(Some("Scala"))
          config.partial should be(false)
      }
    }

    "check a valid url" in {
      configRules.validUrl("https://example.com") should be(true)
      configRules.validUrl("httt://example.com") should be(false)
    }

    "validate report files" in {
      val filesNoneOption = configRules.validateReportFiles(None)
      filesNoneOption should be('right)
      filesNoneOption.right.value should be(List.empty[File])

      val filesSomeInvalid = configRules.validateReportFiles(Some(List.empty[File]))
      filesSomeInvalid should be('left)

      val filesSomeValid = configRules.validateReportFiles(Some(coverageFiles))
      filesSomeValid should be('right)
      filesSomeValid.right.value should be(coverageFiles)
    }

    "get an api base url" in {
      val envVars = Map("CODACY_API_BASE_URL" -> apiBaseUrl)
      val configRulesWithURL = new ConfigurationRules(conf, envVars)
      configRulesWithURL.getApiBaseUrl should be(apiBaseUrl)

      val defaultBaseUrl = configRulesWithURL.publicApiBaseUrl
      val configRulesWithoutURL = new ConfigurationRules(conf, Map.empty)
      configRulesWithoutURL.getApiBaseUrl should be(defaultBaseUrl)
    }
  }

  "validateBaseConfig" should {
    "fail" when {
      def assertFailure(baseCommandConfig: BaseCommandConfig) = {
        val result = configRules.validateBaseConfig(baseCommandConfig)
        result should be('left)
        result
      }

      "no token is used" in {
        val baseConfig =
          BaseCommandConfig(
            None,
            None,
            Option(OrganizationProvider.gh),
            Some("username"),
            Some("projectName"),
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        val result = assertFailure(baseConfig)
        result.left.value should include("project or account API token")
      }

      "project token is empty" in {
        val baseConfig =
          BaseCommandConfig(
            Some(""),
            None,
            None,
            None,
            None,
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        assertFailure(baseConfig)
      }

      "api token is empty" in {
        val baseConfig =
          BaseCommandConfig(
            None,
            Some(""),
            None,
            None,
            None,
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        assertFailure(baseConfig)
      }

      "api token is used and username is not" in {
        val baseConfig =
          BaseCommandConfig(
            None,
            Some("token"),
            None,
            None,
            Some("projectName"),
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        assertFailure(baseConfig)
      }

      "api token is used and project name is not" in {
        val baseConfig =
          BaseCommandConfig(
            None,
            Some("token"),
            None,
            Some("username"),
            None,
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        assertFailure(baseConfig)
      }

      "API URL is invalid" in {
        val baseConfig =
          BaseCommandConfig(
            Some("projectToken"),
            None,
            None,
            None,
            None,
            Some("Invalid URL"),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        assertFailure(baseConfig)
      }

      "commit UUID is not valid" in {
        val baseConfig =
          BaseCommandConfig(Some("token"), None, None, None, None, Some(apiBaseUrl), Some("Commit UUID"))
        assertFailure(baseConfig)
      }
    }

    "succeed" when {
      "project token is used" in {
        val baseConfig =
          BaseCommandConfig(
            Some("token"),
            None,
            None,
            None,
            None,
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        val result = configRules.validateBaseConfig(baseConfig)
        result should be('right)
      }

      "api token and required fields are used" in {
        val baseConfig =
          BaseCommandConfig(
            None,
            Some("apiToken"),
            Option(OrganizationProvider.gh),
            Some("username"),
            Some("projectName"),
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        val result = configRules.validateBaseConfig(baseConfig)
        result should be('right)
      }

      // it should use the project token only
      "project token and api token are used" in {
        val baseConfig =
          BaseCommandConfig(
            Some("projectToken"),
            Some("apiToken"),
            None,
            None,
            None,
            Some(apiBaseUrl),
            Some("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
          )
        val result = configRules.validateBaseConfig(baseConfig)
        result should be('right)
      }
    }
  }
}
