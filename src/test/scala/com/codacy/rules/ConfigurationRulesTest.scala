package com.codacy.rules

import java.io.File

import com.codacy.api.Language
import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.ReportConfig
import org.scalatest.Inside._
import org.scalatest._

class ConfigurationRulesTest extends FlatSpec with Matchers {

  val projToken = "1234adasdsdw333"
  val coverageFile = new File("coverage.xml")
  val apiBaseUrl = "https://api.codacy.com"

  val baseConf = BaseCommandConfig(Some(projToken), Some(apiBaseUrl), None, debug = None)
  val conf = Report(baseConf, "Scala", None, coverageFile, None, None)

  val components = new Components(conf)

  "ConfigurationRules" should "transform configuration" in {
    inside(components.validatedConfig) {
      case config: ReportConfig =>
        config.language should be(Language.Scala)
        config.coverageReport.toString should be("coverage.xml")
    }

  }

  it should "codacyCoverage no parser" in {
    inside(components.validatedConfig) {
      case config: ReportConfig =>
        val result = components.reportRules.coverageWithTokenAndCommit(config)

        result should be(Left("could not parse report, unrecognized report format (tried: Cobertura, Jacoco)"))
    }
  }

}
