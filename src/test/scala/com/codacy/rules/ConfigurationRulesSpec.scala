package com.codacy.rules

import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.ReportConfig
import com.codacy.plugins.api.languages.Languages
import org.scalatest.Inside._
import org.scalatest._

class ConfigurationRulesSpec extends WordSpec with Matchers with OptionValues with EitherValues {

  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://example.com"

  val baseConf = BaseCommandConfig(Some(projToken), Some(apiBaseUrl), None)
  val conf = Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None)

  val components = new Components(conf)

  "ConfigurationRules" should {
    "transform configuration" in {
      inside(components.validatedConfig) {
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
      components.configRules.validUrl("https://example.com") should be(true)
      components.configRules.validUrl("httt://example.com") should be(false)
    }

    "validate report files" in {
      val filesNoneOption = components.configRules.validateReportFiles(None)
      filesNoneOption should be('right)
      filesNoneOption.right.value should be(List.empty[File])

      val filesSomeInvalid = components.configRules.validateReportFiles(Some(List.empty[File]))
      filesSomeInvalid should be('left)

      val filesSomeValid = components.configRules.validateReportFiles(Some(coverageFiles))
      filesSomeValid should be('right)
      filesSomeValid.right.value should be(coverageFiles)
    }

    "get an api base url" in {
      val envVars = Map("CODACY_API_BASE_URL" -> apiBaseUrl)
      val defaultBaseUrl = components.configRules.publicApiBaseUrl

      components.configRules.getApiBaseUrl(envVars) should be(apiBaseUrl)
      components.configRules.getApiBaseUrl(Map.empty) should be(defaultBaseUrl)
    }

    "get the project token" in {
      val envVars = Map("CODACY_PROJECT_TOKEN" -> projToken)

      val validProjectToken = components.configRules.getProjectToken(envVars, false)
      validProjectToken should be('right)
      validProjectToken.right.value should be(projToken)

      val noProjectToken = components.configRules.getProjectToken(Map.empty, false)
      noProjectToken should be('left)
    }
  }
}
