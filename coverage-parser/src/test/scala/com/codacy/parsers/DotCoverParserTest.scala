package com.codacy.parsers

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.implementation.DotcoverParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class DotCoverParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {
  private val nonExistentFile = "coverage-parser/src/test/resources/non-existent.xml"
  private val dotCoverReport = "coverage-parser/src/test/resources/test_dotcover.xml"
  private val differentFormatReport = "coverage-parser/src/test/resources/test_cobertura.xml"
  "parse" should {
    "return an invalid report" when {
      "report file does not exist" in {
        val reader = DotcoverParser.parse(new File("."), new File(nonExistentFile))

        reader shouldBe 'left
      }

      "report file has a different format" in {
        val reader = DotcoverParser.parse(new File("."), new File(differentFormatReport))

        reader shouldBe 'left
      }
    }

    "return a valid report" in {
      val reader = DotcoverParser.parse(new File("."), new File(dotCoverReport))

      reader shouldBe 'right
    }

    "return the expected files" in {
      val reader = DotcoverParser.parse(new File("."), new File(dotCoverReport))
      reader.right.value.fileReports.map(_.filename).sorted shouldBe Seq(
        "src/Coverage/FooBar.cs",
        "src/Tests/FooBarTests.cs",
        "src/Coverage/Program.cs",
        "src/Coverage/Bar.cs",
        "src/Coverage/Foo.cs"
      ).sorted
    }

    "return the expected coverage report" in {
      val reader = DotcoverParser.parse(new File("."), new File(dotCoverReport))

      reader.right.value shouldBe CoverageReport(
        List(
          CoverageFileReport(
            "src/Coverage/FooBar.cs",
            Map(10 -> 1, 21 -> 1, 9 -> 1, 13 -> 0, 17 -> 1, 19 -> 0, 15 -> 0)
          ),
          CoverageFileReport(
            "src/Tests/FooBarTests.cs",
            Map(
              14 -> 1,
              20 -> 1,
              28 -> 1,
              22 -> 1,
              27 -> 1,
              12 -> 1,
              31 -> 1,
              11 -> 1,
              23 -> 1,
              30 -> 1,
              19 -> 1,
              15 -> 1
            )
          ),
          CoverageFileReport("src/Coverage/Program.cs", Map(8 -> 0, 9 -> 0, 10 -> 0)),
          CoverageFileReport(
            "src/Coverage/Bar.cs",
            Map(10 -> 0, 14 -> 1, 9 -> 1, 12 -> 0, 11 -> 0, 8 -> 1, 15 -> 1)
          ),
          CoverageFileReport("src/Coverage/Foo.cs", Map(8 -> 1, 9 -> 1, 10 -> 1))
        )
      )
    }
  }
}
