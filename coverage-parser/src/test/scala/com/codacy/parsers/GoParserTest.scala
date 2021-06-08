package com.codacy.parsers

import java.io.File

import com.codacy.api._


import com.codacy.parsers.implementation.GoParser
import org.scalatest.{EitherValues, Matchers, WordSpec}

class GoParserTest extends WordSpec with Matchers with EitherValues {

  "parse" should {

    "fail to parse an invalid report" when {

      "the report file does not exist" in {
        // Arrange
        val nonExistentReportPath = "coverage-parser/src/test/resources/non-existent.xml"

        // Act
        val parseResult = GoParser.parse(new File("."), new File(nonExistentReportPath))

        // Assert
        parseResult shouldBe Left(
          "Can't load report file."
        )
      }
    }

    "return a valid report" in {
      val reader = GoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_go.out"))

      val testReport = CoverageReport(
        75,
        List(
          CoverageFileReport(
            "example.com/m/v2/hello.go",
            75,
            Map(5 -> 0, 14 -> 1, 6 -> 0, 13 -> 1, 17 -> 1, 12 -> 1, 7 -> 0, 18 -> 1, 11 -> 1, 19 -> 1)
          )
        )
      )

      reader.right.value should equal(testReport)
    }
  }
}
