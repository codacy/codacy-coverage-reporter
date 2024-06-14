package com.codacy.parsers

import java.io.File

import com.codacy.parsers.implementation.CloverParser
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CloverParserTest extends AnyWordSpec with Matchers with EitherValues {

  "parse" should {

    "fail to parse an invalid report" when {

      "the report file does not exist" in {
        // Arrange
        val nonExistentReportPath = "coverage-parser/src/test/resources/non-existent.xml"

        // Act
        val parseResult = CloverParser.parse(new File("."), new File(nonExistentReportPath))

        // Assert
        val error = parseResult.left.value
        error should startWith("Unparseable report. ")
        error should endWith("coverage-parser/src/test/resources/non-existent.xml (No such file or directory)")
      }

      "the report is not in the Clover format" in {
        // Arrange
        val reportNotInCloverFormat = "coverage-parser/src/test/resources/test_cobertura.xml"

        // Act
        val parseResult = CloverParser.parse(new File("."), new File(reportNotInCloverFormat))

        // Assert
        parseResult shouldBe Left("Invalid report. Could not find tag hierarchy <coverage> <project> <metrics> tags.")
      }
    }

    val cloverReportPath = "coverage-parser/src/test/resources/test_clover.xml"
    val cloverWithoutPackagesFilePath = "coverage-parser/src/test/resources/test_clover_without_packages.xml"

    "succeed to parse a valid report" when {

      "the report has packages" in {
        // Act
        val parseResult = CloverParser.parse(new File("."), new File(cloverReportPath))

        // Assert
        parseResult shouldBe Symbol("right")
      }

      "the report does not have packages" in {
        // Act
        val parseResult = CloverParser
          .parse(new File("/home/codacy-php/"), new File(cloverWithoutPackagesFilePath))

        // Assert
        parseResult shouldBe Symbol("right")
      }

    }

    "parse correct file paths" when {

      "reports contain both name and path attributes in file tags" in {
        // Arrange
        val cloverWithPaths = new File("coverage-parser/src/test/resources/test_clover_with_paths.xml")

        // Act
        val parsedReportFilePaths =
          CloverParser
            .parse(new File("/Users/username/workspace/repository"), cloverWithPaths)
            .value
            .fileReports
            .map(_.filename)

        // Assert
        parsedReportFilePaths should contain("src/app/file.js")
      }

    }

    "return the same report with or without packages" in {
      // Act
      val parseResultWithoutPackages = CloverParser
        .parse(new File("/home/codacy-php/"), new File(cloverWithoutPackagesFilePath))
      val parseResultWithPackages =
        CloverParser.parse(new File("/home/codacy-php/"), new File(cloverReportPath))

      // Assert
      parseResultWithoutPackages shouldBe parseResultWithPackages
    }

    "return a report with the expected number of files" in {
      // Arrange
      val expectedNumberOfFiles = 5

      // Act
      val fileReports =
        CloverParser.parse(new File("/home/codacy-php/"), new File(cloverReportPath)).value.fileReports

      // Assert
      fileReports should have length expectedNumberOfFiles
    }

    "return a report with the expected relative file paths" in {
      // Arrange
      val expectedFilePaths = Seq(
        "src/Codacy/Coverage/Parser/Parser.php",
        "src/Codacy/Coverage/Report/CoverageReport.php",
        "vendor/sebastian/global-state/src/Blacklist.php",
        "vendor/sebastian/global-state/src/Restorer.php",
        "vendor/sebastian/global-state/src/Snapshot.php"
      )

      // Act
      val parsedReportFilePaths =
        CloverParser
          .parse(new File("/home/codacy-php/"), new File(cloverReportPath))
          .value
          .fileReports
          .map(_.filename)

      // Assert
      parsedReportFilePaths should contain theSameElementsAs expectedFilePaths
    }

    "return a report with the expected file line coverage" in {
      // Arrange
      val filePath = "src/Codacy/Coverage/Report/CoverageReport.php"

      // Act
      val fileLineCoverage =
        CloverParser
          .parse(new File("/home/codacy-php/"), new File(cloverReportPath))
          .value
          .fileReports
          .find(_.filename == filePath)
          .getOrElse(fail(s"Could not find report for file:$filePath"))
          .coverage

      // Assert
      fileLineCoverage shouldBe Map(
        11 -> 1,
        12 -> 1,
        13 -> 1,
        16 -> 1,
        19 -> 0,
        30 -> 0,
        31 -> 0,
        32 -> 0,
        33 -> 0,
        36 -> 0,
        39 -> 0,
        42 -> 0
      )
    }

  }

}
