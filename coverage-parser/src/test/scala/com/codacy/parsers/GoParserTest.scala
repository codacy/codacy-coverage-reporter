package com.codacy.parsers

import java.io.File
import com.codacy.api._
import com.codacy.parsers.implementation.GoParser
import org.scalatest.{EitherValues, Matchers, WordSpec}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class GoParserTest extends WordSpec with Matchers with EitherValues {

  "parse" should {

    "fail to parse an invalid report" when {

      "the report file does not exist" in {
        // Arrange
        val nonExistentReportPath = "coverage-parser/src/test/resources/non-existent.xml"

        // Act
        val parseResult = GoParser.parse(new File("."), new File(nonExistentReportPath))

        // Assert
        parseResult shouldBe Left("Can't load report file.")
      }
    }

    "return a valid report" in {
      val reader = GoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_go.out"))

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "example.com/m/v2/hello.go",
            Map(5 -> 0, 14 -> 1, 6 -> 0, 13 -> 1, 17 -> 1, 12 -> 1, 7 -> 0, 18 -> 1, 11 -> 1, 19 -> 1)
          )
        )
      )

      mergeReport(reader.right.value.fileReports) should equal(testReport)
    }
  }

  def mergeReport(fileReports: Seq[CoverageFileReport]) = {

    val merged = fileReports.groupBy((a: CoverageFileReport) => a.filename).map {
      case (str, reports) =>
        (str, reports.map(_.coverage).toList.flatten.toMap)

    }
    val mergedFileReports = merged.map {
      case (str, intToInt) => CoverageFileReport(str, intToInt)
    }.toList

    CoverageReport(mergedFileReports)
  }

  "Coverage calculated by go coverage tool VS Codacy coverage" in {
    def parseRawReport() = {
      val report = Try(Source.fromFile(new File("coverage-parser/src/test/resources/go/asyncport.out"))) match {
        case Success(lines) =>
          Right(lines.getLines)
        case Failure(ex) =>
          Left("Can't load report file.")
      }
      GoParser.parseAllCoverageInfo(report.right.value.toList)
    }

    val tuple = parseRawReport().foldLeft((0, 0))((acc, next) => {
      val covered = if (next.countOfStatements > 0) {
        next.numberOfStatements
      } else 0
      (acc._1 + next.numberOfStatements, acc._2 + covered)
    })

    val reader = GoParser.parse(new File("."), new File("coverage-parser/src/test/resources/go/asyncport.out"))

    val report1 = mergeReport(reader.right.value.fileReports)

    val goToolCoverage = tuple._2.toDouble / tuple._1.toDouble

    val codacyCoverage = report1.fileReports.head.coverage.values
      .count(_ > 0)
      .toDouble / report1.fileReports.head.coverage.keys.size.toDouble

    println(s"go coverage: $goToolCoverage")
    println(s"codacy coverage: $codacyCoverage")
    //If this fails it means that you fixed something
    goToolCoverage shouldNot equal(codacyCoverage)
  }
}
