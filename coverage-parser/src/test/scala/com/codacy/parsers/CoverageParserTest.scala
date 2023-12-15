package com.codacy.parsers

import java.io.File
import com.codacy.parsers.implementation._

import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class CoverageParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {
  private val coberturaReportPath = "coverage-parser/src/test/resources/test_cobertura.xml"
  private val cloverReportPath = "coverage-parser/src/test/resources/test_clover.xml"

  "parse" should {
    "return the specific error" when {
      "the file cannot be parsed with a specific parser" in {
        val reader = CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(CloverParser), Seq.empty)

        reader shouldBe 'left
      }
      "the file cannot be parsed with another specific parser" in {
        val reader = CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(LCOVParser), Seq.empty)

        reader shouldBe 'left
      }
    }
    "return a valid result" when {
      "file and format are matching cobertura" in {
        val reader =
          CoverageParser.parse(new File("."), new File(coberturaReportPath), Some(CoberturaParser), Seq.empty)

        reader shouldBe 'right
      }
      "file and format are matching clover" in {
        val reader = CoverageParser.parse(new File("."), new File(cloverReportPath), Some(CloverParser), Seq.empty)

        reader shouldBe 'right
      }
    }
  }
}
