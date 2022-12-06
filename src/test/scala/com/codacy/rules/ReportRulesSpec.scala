package com.codacy.rules

import java.io.File

import com.codacy.api.client.{FailedResponse, RequestSuccess, RequestTimeout, SuccessfulResponse}
import com.codacy.api.service.CoverageServices
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.{BaseConfig, ProjectTokenAuthenticationConfig, ReportConfig}
import com.codacy.plugins.api.languages.Languages
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest._

class ReportRulesSpec extends WordSpec with Matchers with PrivateMethodTester with EitherValues with IdiomaticMockito {
  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://api.codacy.com"

  val baseConf = BaseCommandConfig(Some(projToken), None, None, None, None, Some(apiBaseUrl), None, 10000, 3)

  val conf =
    Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None, forceCoverageParser = None)

  val coverageReport = CoverageReport(100, Seq(CoverageFileReport("file.scala", 100, Map(10 -> 1))))
  val noLanguageReport = CoverageReport(0, Seq.empty[CoverageFileReport])

  val configRules = new ConfigurationRules(conf, sys.env)
  val validatedConfig = configRules.validatedConfig.right.value

  val components = new Components(validatedConfig)

  "codacyCoverage" should {
    val baseConfig =
      BaseConfig(
        ProjectTokenAuthenticationConfig(projToken),
        apiBaseUrl,
        None,
        debug = false,
        timeout = RequestTimeout(1000, 10000),
        sleepTime = 10000,
        numRetries = 3
      )

    def assertCodacyCoverage(
        coverageServices: CoverageServices,
        coverageReports: List[String],
        success: Boolean,
        projectFiles: Option[List[File]] = None
    ) = {
      val reportRules = new ReportRules(coverageServices)
      val reportConfig =
        ReportConfig(
          baseConfig,
          None,
          forceLanguage = false,
          coverageReports = coverageReports.map(new File(_)),
          partial = false,
          prefix = "",
          forceCoverageParser = None
        )
      val result = projectFiles match {
        case Some(files) =>
          reportRules.codacyCoverage(reportConfig, files)
        case None =>
          reportRules.codacyCoverage(reportConfig)
      }

      result should be(if (success) 'right else 'left)
    }

    "fail" when {
      "it finds no report file" in {
        val coverageServices = mock[CoverageServices]

        assertCodacyCoverage(coverageServices, List(), success = false, projectFiles = Some(List.empty))
      }

      "it is not able to parse report file" in {
        val coverageServices = mock[CoverageServices]

        assertCodacyCoverage(coverageServices, List("src/test/resources/invalid-report.xml"), success = false)
      }

      "cannot send report" in {
        val coverageServices = mock[CoverageServices]

        coverageServices.sendReport(
          any[String],
          any[String],
          any[CoverageReport],
          anyBoolean,
          Some(RequestTimeout(1000, 10000)),
          Some(10000),
          Some(3)
        ) returns FailedResponse("Failed to send report")

        assertCodacyCoverage(coverageServices, List("src/test/resources/dotcover-example.xml"), success = false)
      }
    }

    "succeed if it can parse and send the report" in {
      val coverageServices = mock[CoverageServices]

      coverageServices.sendReport(
        any[String],
        any[String],
        any[CoverageReport],
        anyBoolean,
        Some(RequestTimeout(1000, 10000)),
        Some(10000),
        Some(3)
      ) returns SuccessfulResponse(RequestSuccess("Success"))

      assertCodacyCoverage(coverageServices, List("src/test/resources/dotcover-example.xml"), success = true)
    }
  }

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
      val langEither = components.reportRules.guessReportLanguage(
        languageOpt = conf.language,
        report = noLanguageReport,
        reportFilePath = "I/am/a/file.extension"
      )

      langEither should be('right)
      langEither.right.value should be(conf.language.get)
    }

    "provide the Scala language from report" in {
      val langEither = components.reportRules
        .guessReportLanguage(languageOpt = None, report = coverageReport, reportFilePath = "I/am/a/file.extension")

      langEither should be('right)
      langEither.right.value should be(Languages.Scala.name)
    }

    "not provide the language from an empty report" in {
      components.reportRules.guessReportLanguage(
        languageOpt = None,
        report = noLanguageReport,
        reportFilePath = "I/am/a/file.extension"
      ) should be('left)
    }
  }

  "guessReportFiles" should {
    "provide a report file" in {
      val projectFiles = List(new File("jacoco-coverage.xml"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be('right)
      reportEither.right.value.map(_.toString) should be(List("jacoco-coverage.xml"))
    }

    "not provide a report file" in {
      val projectFiles = List(new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be('left)
    }

    "provide the available report file" in {
      val files = List(new File("coverage.xml"))
      val reportEither = components.reportRules.guessReportFiles(files, List.empty)

      reportEither should be('right)
      reportEither.right.value should be(files)
    }

    "only provide phpunit report file inside coverage-xml" in {
      val projectFiles = List(new File("index.xml"), new File("coverage-xml", "index.xml"))
      val reportEither = components.reportRules.guessReportFiles(List.empty, projectFiles)

      reportEither should be('right)
      reportEither.right.value should be(List(new File("coverage-xml", "index.xml")))
    }

    "find an lcov report" in {
      val projectFiles =
        List(new File("lcov.info"), new File("lcov.dat"), new File("foo.lcov"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be('right)
      reportEither.right.value.map(_.toString) should be(List("lcov.info", "lcov.dat", "foo.lcov"))
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
        val emptyReport = CoverageReport(0, List(CoverageFileReport("file-name", 0, Map.empty)))
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
