package com.codacy.rules

import java.io.File

import com.codacy.api.client.{FailedResponse, RequestSuccess, SuccessfulResponse}
import com.codacy.api.service.CoverageServices
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.{BaseConfig, CommitUUID, ProjectTokenAuthenticationConfig, ReportConfig}
import com.codacy.plugins.api.languages.Languages
import com.codacy.repositories.CoverageServiceRepository
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest._

class ReportRulesSpec extends WordSpec with Matchers with PrivateMethodTester with EitherValues with IdiomaticMockito {
  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://api.codacy.com"

  val commitUUID = CommitUUID("commitUUID")

  val baseConf = BaseCommandConfig(Some(projToken), None, None, None, Some(apiBaseUrl), None)
  val conf = Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None)

  val coverageReport = CoverageReport(100, Seq(CoverageFileReport("file.scala", 100, Map(10 -> 1))))
  val noLanguageReport = CoverageReport(0, Seq.empty[CoverageFileReport])

  val configRules = new ConfigurationRules(conf)
  val validatedConfig = configRules.validatedConfig.right.value

  val components = new Components(validatedConfig)

  "codacyCoverage" should {
    val baseConfig =
      BaseConfig(
        ProjectTokenAuthenticationConfig(projToken),
        apiBaseUrl,
        Some(commitUUID),
        debug = false,
        skipSend = false
      )

    def assertCodacyCoverage(coverageServices: CoverageServices, coverageReports: List[String], success: Boolean) = {
      val reportRules = new ReportRules(new CoverageServiceRepository(coverageServices))
      val reportConfig =
        ReportConfig(
          baseConfig,
          None,
          forceLanguage = false,
          coverageReports = coverageReports.map(new File(_)),
          partial = false,
          prefix = ""
        )
      val result = reportRules.codacyCoverage(reportConfig)

      result should be(if (success) 'right else 'left)
    }

    "fail" when {
      "it finds no report file" in {
        val coverageServices = mock[CoverageServices]

        assertCodacyCoverage(coverageServices, List(), success = false)
      }

      "it is not able to parse report file" in {
        val coverageServices = mock[CoverageServices]

        assertCodacyCoverage(coverageServices, List("src/test/resources/invalid-report.xml"), success = false)
      }

      "cannot send report" in {
        val coverageServices = mock[CoverageServices]

        coverageServices.sendReport(any[String], any[String], any[CoverageReport], anyBoolean) returns FailedResponse(
          "Failed to send report"
        )

        assertCodacyCoverage(coverageServices, List("src/test/resources/dotcover-example.xml"), success = false)
      }
    }

    "succeed if it can parse and send the report" in {
      val coverageServices = mock[CoverageServices]

      coverageServices.sendReport(any[String], any[String], any[CoverageReport], anyBoolean) returns SuccessfulResponse(
        RequestSuccess("Success")
      )

      assertCodacyCoverage(coverageServices, List("src/test/resources/dotcover-example.xml"), success = true)
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

    "successfully store report" when {
      def storeValidReport() = {
        val emptyReport = CoverageReport(0, List(CoverageFileReport("file-name", 0, Map())))
        val tempFile = File.createTempFile("storeReport", "not-store")
        components.reportRules.storeReport(emptyReport, tempFile)
      }

      "store is successful" in {
        val result = storeValidReport()
        result should be('right)
      }

      "store report exists" in {
        val result = storeValidReport()
        val resultFile = new File(result.right.value)
        resultFile should be('exists)
      }
    }
  }
}
