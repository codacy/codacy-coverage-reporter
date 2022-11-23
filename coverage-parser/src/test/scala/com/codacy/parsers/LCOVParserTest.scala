package com.codacy.parsers

import java.io.File

import com.codacy.api._
import com.codacy.parsers.implementation.LCOVParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class LCOVParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {

  "LCOVParser" should {

    "identify if report is invalid" in {
      val reader = LCOVParser.parse(new File("."), new File("coverage-parser/src/test/resources/invalid_report.lcov"))
      reader.isLeft shouldBe true
    }

    "identify if report is invalid beacuse is cobertura format" in {
      val reader = LCOVParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"))
      reader.isLeft shouldBe true
    }

    "identify if report is invalid beacuse is clover format" in {
      val reader = LCOVParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_clover.xml"))
      reader.isLeft shouldBe true
    }

    "identify if report is valid" in {
      val reader = LCOVParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_lcov.lcov"))
      reader.isRight shouldBe true
    }

    "return a valid report" in {
      val reader = LCOVParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_lcov.lcov"))

      val testReport = CoverageReport(
        0,
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            0,
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            0,
            Map(3 -> 0, 4 -> 1, 5 -> 1, 6 -> 2)
          )
        )
      )

      reader.right.value should equal(testReport)
    }

  }

}
