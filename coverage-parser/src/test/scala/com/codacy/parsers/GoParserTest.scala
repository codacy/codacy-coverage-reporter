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
          "Can't load report file. coverage-parser/src/test/resources/non-existent.xml"
        )
      }
    }

    "return a valid report" in {
      val reader = GoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_go.out"))

      val testReport = CoverageReport(
        86,
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            100,
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            75,
            Map(3 -> 0, 4 -> 1, 5 -> 1, 6 -> 2)
          )
        )
      )

      reader.right.value should equal(testReport)
    }


  }
}
