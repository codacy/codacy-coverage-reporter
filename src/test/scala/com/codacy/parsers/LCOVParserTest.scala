package com.codacy.parsers

import java.io.File

import com.codacy.api._
import com.codacy.parsers.implementation.LCOVParser
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LCOVParserTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with EitherValues {

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
        List(
          CoverageFileReport("coverage-parser/src/test/resources/TestSourceFile2.scala", Map(1 -> 1, 2 -> 1, 3 -> 1)),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            Map(3 -> 0, 4 -> 1, 5 -> 1, 6 -> 2)
          )
        )
      )

      reader.value should equal(testReport)
    }

  }

}
