package com.codacy.parsers

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CoverageParserFactoryTest extends WordSpec with BeforeAndAfterAll with Matchers {

  "CoverageParserFactory" should {

    "get report with unspecified parser" in {
      val expectedReport = CoverageReport(
        List(
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile2.scala",
            Map(1 -> 1, 2 -> 1, 3 -> 1)
          ),
          CoverageFileReport(
            "coverage-parser/src/test/resources/TestSourceFile.scala",
            Map(5 -> 1, 10 -> 1, 6 -> 2, 9 -> 1, 3 -> 0, 4 -> 1)
          )
        )
      )

      CoverageParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml")) shouldEqual Right(
        expectedReport
      )
    }

    "get report with jacoco parser" in {
      val expectedReport = CoverageReport(
        List(
          CoverageFileReport(
            "org/eluder/coverage/sample/InnerClassCoverage.java",
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          ),
          CoverageFileReport("org/eluder/coverage/sample/SimpleCoverage.java", Map(3 -> 1, 6 -> 1, 10 -> 0, 11 -> 0))
        )
      )

      CoverageParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml")) shouldEqual Right(
        expectedReport
      )
    }

    "fail to get invalid report" in {
      CoverageParser.parse(new File("."), new File("invalid_report.xml")) shouldEqual Left(
        "Could not parse report, unrecognized report format (tried: Cobertura, Jacoco, Clover, OpenCover, DotCover, PHPUnit, LCOV, Go)"
      )
    }
  }
}
