package com.codacy.parsers

import java.io.File

import com.codacy.api._
import com.codacy.parsers.implementation.JacocoParser
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}

class JacocoParserTest extends WordSpec with BeforeAndAfterAll with Matchers with EitherValues {

  "JacocoParser" should {

    "identify if report is invalid" in {
      val reader = JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"))
      reader.isLeft shouldBe true
    }

    "identify if report is valid" in {
      val reader = JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"))
      reader.isRight shouldBe true
    }

    "return a valid report" in {
      val reader = JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"))

      val testReport = CoverageReport(
        73,
        List(
          CoverageFileReport(
            "org/eluder/coverage/sample/InnerClassCoverage.java",
            81,
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          ),
          CoverageFileReport(
            "org/eluder/coverage/sample/SimpleCoverage.java",
            50,
            Map(3 -> 1, 6 -> 1, 10 -> 0, 11 -> 0)
          )
        )
      )

      reader.right.value should equal(testReport)
    }

  }

}
