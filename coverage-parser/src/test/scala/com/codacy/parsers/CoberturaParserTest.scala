package com.codacy.parsers

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.implementation.CoberturaParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class CoberturaParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {

  "CoberturaParser" should {

    "identify if report is invalid" in {
      val reader = CoberturaParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"))
      reader.isLeft shouldBe true
    }

    "identify if report is valid" in {
      val reader =
        CoberturaParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"))
      reader.isRight shouldBe true
    }

    "return a valid report" in {
      val reader =
        CoberturaParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"))

      val testReport = CoverageReport(
        87,
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            87,
            Map(5 -> 1, 10 -> 1, 6 -> 2, 9 -> 1, 3 -> 0, 4 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            87,
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          )
        )
      )

      reader.right.value should equal(testReport)
    }

    "no crash on thousands separators" in {
      val reader =
        CoberturaParser.parse(new File("."), new File("coverage-parser/src/test/resources/thousand_sep_cobertura.xml"))

      val testReport = CoverageReport(
        87,
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            87,
            Map(5 -> 1, 10 -> 1, 6 -> 2, 9 -> 1, 9 -> 0, 8 -> 1, 4 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            87,
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          ),
        )
      )

      reader.right.value should equal(testReport)
    }

    "return a valid report with windows file path separator" in {
      val reader =
        CoberturaParser.parse(new File("."), new File("coverage-parser/src/test/resources/windows_paths_cobertura.xml"))

      val testReport = CoverageReport(
        87,
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            87,
            Map(5 -> 1, 10 -> 1, 6 -> 2, 9 -> 1, 3 -> 0, 4 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            87,
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          ),
        )
      )

      reader.right.value should equal(testReport)
    }

  }

}
