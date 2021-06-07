package com.codacy.parsers

import java.io.File

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
  }
}
