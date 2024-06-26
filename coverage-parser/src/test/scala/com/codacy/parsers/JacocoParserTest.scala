package com.codacy.parsers

import java.io.File

import com.codacy.api._
import com.codacy.parsers.implementation.JacocoParser
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class JacocoParserTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with EitherValues {

  "JacocoParser" should {

    "identify if report is invalid" in {
      val reader =
        JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_cobertura.xml"))
      reader.isLeft shouldBe true
    }

    "identify if report is valid" in {
      val reader =
        JacocoParser.parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"))
      reader.isRight shouldBe true
    }

    "return a valid report" in {

      val reader = JacocoParser
        .parse(new File("."), new File("coverage-parser/src/test/resources/test_jacoco.xml"))

      val testReport = CoverageReport(
        List(
          CoverageFileReport(
            "org/eluder/coverage/sample/InnerClassCoverage.java",
            Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 27 -> 0, 12 -> 1, 3 -> 1, 16 -> 1, 26 -> 0, 19 -> 1)
          ),
          CoverageFileReport("org/eluder/coverage/sample/SimpleCoverage.java", Map(3 -> 1, 6 -> 1, 10 -> 0, 11 -> 0))
        )
      )

      reader.value should equal(testReport)
    }
  }

}
