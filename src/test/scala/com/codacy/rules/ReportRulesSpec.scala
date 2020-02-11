package com.codacy.rules

import org.scalatest._

import java.io.File

import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.api.client.FailedResponse
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.plugins.api.languages.Languages

class ReportRulesSpec extends WordSpec with Matchers with PrivateMethodTester with EitherValues {
  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://api.codacy.com"

  val baseConf = BaseCommandConfig(Some(projToken), Some(apiBaseUrl), None)
  val conf = Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None)

  val coverageReport = CoverageReport(100, Seq(CoverageFileReport("file.scala", 100, Map(10 -> 1))))
  val noLanguageReport = CoverageReport(0, Seq.empty[CoverageFileReport])

  val components = new Components(conf)

  "handleFailedResponse" should {
    "provide a different message" in {
      val notFoundMessage = FailedResponse("not found")
      components.reportRules.handleFailedResponse(notFoundMessage) should not be notFoundMessage.message
    }

    "provide the same message" in {
      val unknownErrorMessage = FailedResponse("unknown error")
      components.reportRules.handleFailedResponse(unknownErrorMessage) should be(unknownErrorMessage.message)
    }
  }

  "guessReportLanguage" should {
    "provide the available language" in {
      val langEither = components.reportRules.guessReportLanguage(conf.language, noLanguageReport)

      langEither should be('right)
      langEither.right.value should be(conf.language.get)
    }

    "provide the Scala language from report" in {
      val langEither = components.reportRules.guessReportLanguage(None, coverageReport)

      langEither should be('right)
      langEither.right.value should be(Languages.Scala.name)
    }

    "not provide the language from an empty report" in {
      components.reportRules.guessReportLanguage(None, noLanguageReport) should be('left)
    }
  }

  "guessReportFiles" should {
    "provide a report file" in {
      val fileIterator = Iterator(new File("jacoco-coverage.xml"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], fileIterator)

      reportEither should be('right)
      reportEither.right.value.map(_.toString) should be(List("jacoco-coverage.xml"))
    }

    "not provide a report file" in {
      val fileIterator = Iterator(new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], fileIterator)

      reportEither should be('left)
    }

    "provide the available report file" in {
      val files = List(new File("coverage.xml"))
      val reportEither = components.reportRules.guessReportFiles(files, Iterator.empty)

      reportEither should be('right)
      reportEither.right.value should be(files)
    }

    "only provide phpunit report file inside coverage-xml" in {
      val fileIterator = Iterator(new File("index.xml"), new File("coverage-xml", "index.xml"))
      val reportEither = components.reportRules.guessReportFiles(List.empty, fileIterator)

      reportEither should be('right)
      reportEither.right.value should be(List(new File("coverage-xml", "index.xml")))
    }
  }

  "validateFileAccess" should {
    "not validate file" when {
      "file does not exist" in {
        val file = new File("not-exist.xml")
        val result = components.reportRules.validateFileAccess(file)

        result should be('left)
      }

      "file does not have read access" in {
        val file = File.createTempFile("validateFileAccess", "read-access")
        file.createNewFile()
        file.setReadable(false)
        file.deleteOnExit()

        val result = components.reportRules.validateFileAccess(file)
        result should be('left)
      }
    }

    "validate file" in {
      val file = File.createTempFile("validateFileAccess", "valid")
      file.createNewFile()
      file.deleteOnExit()

      val result = components.reportRules.validateFileAccess(file)
      result should be('right)
    }
  }

  "storeReport" should {
    "not store report" in {
      val emptyReport = CoverageReport(0, Seq.empty[CoverageFileReport])
      val tempFile = File.createTempFile("storeReport", "not-store")
      val result = components.reportRules.storeReport(emptyReport, tempFile)

      result should be('left)
    }

    "report is stored" when {
      def storeValidReport() = {
        val emptyReport = CoverageReport(0, List(CoverageFileReport("file-name", 0, Map())))
        val tempFile = File.createTempFile("storeReport", "not-store")
        components.reportRules.storeReport(emptyReport, tempFile)
      }

      "store is successful" in {
        val result = storeValidReport()
        result should be('right)
      }

      "store report" in {
        val result = storeValidReport()
        val resultFile = new File(result.right.value)
        resultFile.exists should be(true)
      }
    }
  }
}
