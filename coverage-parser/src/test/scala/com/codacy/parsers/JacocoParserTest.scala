package com.codacy.parsers

import java.io.File

import com.codacy.api._
import com.codacy.parsers.implementation.JacocoParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class JacocoParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {

  "JacocoParser" should {

    "identify if report is invalid" in {
      val reader =
        JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"), Seq.empty)
      reader.isLeft shouldBe true
    }

    "identify if report is valid" in {
      val reader =
        JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"), Seq.empty)
      reader.isRight shouldBe true
    }

    "return a valid report" in {

      val acceptableFiles =
        Seq("org/eluder/coverage/sample/InnerClassCoverage.java", "org/eluder/coverage/sample/SimpleCoverage.java")

      val reader = JacocoParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"), acceptableFiles)

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "org/eluder/coverage/sample/InnerClassCoverage.java",
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          ),
          CoverageFileReport("org/eluder/coverage/sample/SimpleCoverage.java", Map(3 -> 1, 6 -> 1, 10 -> 0, 11 -> 0))
        )
      )

      reader.right.value should equal(testReport)
    }

    "return a valid report, only files present in the acceptable files" in {

      val acceptableFiles =
        Seq("org/eluder/coverage/sample/InnerClassCoverage.java")

      val reader = JacocoParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"), acceptableFiles)

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "org/eluder/coverage/sample/InnerClassCoverage.java",
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          )
        )
      )

      reader.right.value should equal(testReport)
    }

    "return a valid report, file name is updated to match acceptable file names" in {

      val acceptableFiles =
        Seq("src/java/org/eluder/coverage/sample/InnerClassCoverage.java")

      val reader = JacocoParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"), acceptableFiles)

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "src/java/org/eluder/coverage/sample/InnerClassCoverage.java",
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          )
        )
      )

      reader.right.value should equal(testReport)
    }

  }

}
