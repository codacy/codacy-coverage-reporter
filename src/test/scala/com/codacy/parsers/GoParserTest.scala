package com.codacy.parsers

import java.io.File

import com.codacy.api._

import com.codacy.parsers.implementation.GoParser
import org.scalatest.{EitherValues}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GoParserTest extends AnyWordSpec with Matchers with EitherValues {

  "parse" should {

    "fail to parse an invalid report" when {

      "the report file does not exist" in {
        // Arrange
        val nonExistentReportPath = "src/test/resources/non-existent.xml"

        // Act
        val parseResult = GoParser.parse(new File("."), new File(nonExistentReportPath))

        // Assert
        parseResult shouldBe Left("Can't load report file.")
      }
    }

    "return a valid report" in {
      val reader = GoParser.parse(new File("."), new File("src/test/resources/test_go.out"))

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "example.com/m/v2/hello.go",
            Map(5 -> 0, 14 -> 1, 6 -> 0, 13 -> 1, 17 -> 1, 12 -> 1, 7 -> 0, 18 -> 1, 11 -> 1, 19 -> 1)
          )
        )
      )

      reader.value should equal(testReport)
    }

    "return consistent values" in {
      //given two reports where the package names were only changed, should return the same results COV-207
      val reader = GoParser.parse(new File("."), new File("src/test/resources/go/original_package.out"))
      val reader1 =
        GoParser.parse(new File("."), new File("src/test/resources/go/changed_package_name.out"))

      reader.value.fileReports(0).coverage should equal(reader1.value.fileReports(0).coverage)
    }
  }
}
