package com.codacy

import java.io.File

import com.codacy.api.Language
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import org.scalatest._

class CodacyCoverageReporterTest extends FlatSpec with Matchers {

  val projToken = "1234adasdsdw333"
  val coverageFile = new File("coverage.xml")
  val apiBaseUrl = "https://api.codacy.com"
  val prefix = ""

  val args = Array("-l", "Scala", "-r", "coverage.xml")

  lazy val reportRules = CodacyCoverageReporter.reportRules
  lazy val configRules = CodacyCoverageReporter.configRules


  "CodacyCoverageReporter" should "buildParser" in {

    val result = configRules.parseConfig(args)

    result.value.language should be(Language.Scala)
    result.value.coverageReport.toString should be("coverage.xml")
  }

  it should "codacyCoverage no parser" in {

    configRules.parseConfig(args) match {
      case Some(conf) =>
        val result = reportRules.coverageWithTokenAndCommit(conf)
        result.left.value should be("no parser for Scala")
      case _ =>
        fail("args parser error")
    }
  }

}
