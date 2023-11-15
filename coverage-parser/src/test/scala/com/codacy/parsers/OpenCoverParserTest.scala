package com.codacy.parsers

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.implementation.OpenCoverParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class OpenCoverParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {
  private val openCoverReportPath = "coverage-parser/src/test/resources/test_opencover.xml"
  private val nonExistentReportPath = "coverage-parser/src/test/resources/non_existent.xml"
  private val coberturaReportPath = "coverage-parser/src/test/resources/test_cobertura.xml"
  "parse" should {
    "return an invalid report" when {
      "report file does not exist" in {
        val reader = OpenCoverParser.parse(new File("."), new File(nonExistentReportPath))
        reader shouldBe 'left
      }

      "report file has a different format" in {
        val reader = OpenCoverParser.parse(new File("."), new File(coberturaReportPath))
        reader shouldBe 'left
      }
    }

    "return a valid report" in {
      val reader = OpenCoverParser.parse(new File("."), new File(openCoverReportPath))
      reader shouldBe 'right
    }

    "return the expected files" in {
      val reader = OpenCoverParser.parse(new File("."), new File(openCoverReportPath))

      reader.right.value.fileReports.map(_.filename).sorted shouldBe Seq("bar.cs", "foo.cs", "foobar.cs").sorted
    }

    "return the expected report" in {
      val reader = OpenCoverParser.parse(new File("."), new File(openCoverReportPath))

      reader.right.value shouldBe CoverageReport(
        List(
          CoverageFileReport("foo.cs", Map(10 -> 1)),
          CoverageFileReport("bar.cs", Map(10 -> 0)),
          CoverageFileReport("foobar.cs", Map(10 -> 0, 20 -> 1))
        )
      )
    }
  }
}
