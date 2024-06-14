package com.codacy.parsers

import java.io.File

import com.codacy.parsers.implementation.PhpUnitXmlParser
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PhpUnitXmlParserTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with EitherValues {
  private val rootPath = "/home/codacy-php/"
  private val validReport = "src/test/resources/phpunitxml/index.xml"
  private val incorrectReport = "src/test/resources/phpunitxml/incorrect_index.xml"
  private val coberturaReport = "src/test/resources/test_cobertura.xml"
  private val nonExistentReport = "src/test/resources/non_existent_file.xml"
  private val configPhpFile = "Config.php"

  "parse" should {
    "return an invalid report" when {
      "report file does not exist" in {
        val reader = PhpUnitXmlParser.parse(new File("."), new File(nonExistentReport))

        reader shouldBe Symbol("left")
      }

      "report file has a different format" in {
        // use some coverage file that does not follow the PHPUnit xml format
        val reader = PhpUnitXmlParser.parse(new File("."), new File(coberturaReport))

        reader shouldBe Symbol("left")
      }

      "report refers to non-existent file coverage report" in {
        // this index contains a reference to a file that includes references to non-existent files
        val reader =
          PhpUnitXmlParser.parse(new File("."), new File(incorrectReport))

        reader.isLeft shouldBe true
      }
    }

    "verify if report is valid" in {
      val reader = PhpUnitXmlParser
        .parse(new File(rootPath), new File(validReport))

      reader shouldBe Symbol("right")
    }

    "return a report with the expected number of files" in {
      val report = PhpUnitXmlParser
        .parse(new File(rootPath), new File(validReport))
        .value

      report.fileReports.length shouldBe 10
    }

    "return a report with the expected file names" in {
      val report = PhpUnitXmlParser
        .parse(new File(rootPath), new File(validReport))
        .value

      report.fileReports.map(_.filename).sorted shouldBe Seq(
        "src/Codacy/Coverage/Api/Api.php",
        "src/Codacy/Coverage/CodacyPhpCoverage.php",
        "src/Codacy/Coverage/Config.php",
        "src/Codacy/Coverage/Git/GitClient.php",
        "src/Codacy/Coverage/Parser/CloverParser.php",
        "src/Codacy/Coverage/Parser/Parser.php",
        "src/Codacy/Coverage/Parser/PhpUnitXmlParser.php",
        "src/Codacy/Coverage/Report/CoverageReport.php",
        "src/Codacy/Coverage/Report/FileReport.php",
        "src/Codacy/Coverage/Report/JsonProducer.php"
      ).sorted
    }

    "return a report with the expected line coverage" in {
      val report = PhpUnitXmlParser
        .parse(new File(rootPath), new File(validReport))
        .value

      report.fileReports.find(_.filename.endsWith(configPhpFile)) match {
        case None => fail(configPhpFile + " file is not present in the list of file reports")
        case Some(fileReport) =>
          fileReport.coverage shouldBe Map(24 -> 4, 25 -> 4, 26 -> 4, 27 -> 4, 28 -> 4, 29 -> 4)
      }
    }
  }
}
