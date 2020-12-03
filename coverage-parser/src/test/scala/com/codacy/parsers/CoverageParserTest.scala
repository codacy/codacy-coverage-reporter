package com.codacy.parsers

import java.io.File
import com.codacy.parsers.implementation._

import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class CoverageParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {
  private val coberturaReportPath = "src/test/resources/test_cobertura.xml"
  private val cloverReportPath = "src/test/resources/test_clover.xml"

  "parse" should {
    "return the specific error" when {
      "the file cannot be parsed with a specific parser" in {
        val reader = CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(CloverParser))

        reader shouldBe 'left
      }
      "the file cannot be parsed with another specific parser" in {
        val reader = CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(LCOVParser))

        reader shouldBe 'left
      }
    }
    "return a valid result" when {
      "file and format are matching cobertura" in {
        val reader = CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(CoberturaParser))

        reader shouldBe 'right
      }
      "file and format are matching clover" in {
        val reader = CoverageParser.parse(new File("."), new File(cloverReportPath), Some(CloverParser))

        reader shouldBe 'right
      }
    }
  }
}
