package com.codacy.rules

import java.io.File
import com.codacy.api.client.{FailedResponse, RequestSuccess, RequestTimeout, SuccessfulResponse}
import com.codacy.api.service.CoverageServices
import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.configuration.parser.{BaseCommandConfig, Report}
import com.codacy.di.Components
import com.codacy.model.configuration.{BaseConfig, ProjectTokenAuthenticationConfig, ReportConfig}
import com.codacy.plugins.api.languages.Languages
import com.codacy.rules.file.GitFileFetcher
import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory

class ReportRulesSpec extends AnyWordSpec with Matchers with PrivateMethodTester with EitherValues with MockFactory {
  val projToken = "1234adasdsdw333"
  val coverageFiles = List(new File("coverage.xml"))
  val apiBaseUrl = "https://api.codacy.com"

  val baseConf = BaseCommandConfig(Some(projToken), None, None, None, None, Some(apiBaseUrl), None, 10000, 3)

  val conf =
    Report(baseConf, Some("Scala"), coverageReports = Some(coverageFiles), prefix = None, forceCoverageParser = None)

  val coverageReport = CoverageReport(Seq(CoverageFileReport("file.scala", Map(10 -> 1))))
  val noLanguageReport = CoverageReport(Seq.empty[CoverageFileReport])

  val configRules = new ConfigurationRules(conf, sys.env)
  val validatedConfig = configRules.validatedConfig.value

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
        gitFileFetcher: GitFileFetcher,
        coverageReports: List[String],
        success: Boolean,
        projectFiles: Option[List[File]] = None
    ) = {
      val reportRules = new ReportRules(coverageServices, gitFileFetcher)
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

      result should be(if (success) Symbol("right") else Symbol("left"))
    }

    "fail" when {
      "it finds no report file" in {
        val coverageServices = stub[CoverageServices]
        val gitFileFetcher = stub[GitFileFetcher]

        assertCodacyCoverage(coverageServices, gitFileFetcher, List(), success = false, projectFiles = Some(List.empty))
      }

      "it is not able to parse report file" in {
        val coverageServices = stub[CoverageServices]
        val gitFileFetcher = stub[GitFileFetcher]

        (gitFileFetcher.forCommit _).when(*).returns(Right(Seq.empty))

        assertCodacyCoverage(
          coverageServices,
          gitFileFetcher,
          List("src/test/resources/invalid-report.xml"),
          success = false
        )
      }

      "cannot send report" in {
        val coverageServices = stub[CoverageServices]
        val gitFileFetcher = stub[GitFileFetcher]

        (gitFileFetcher.forCommit _).when(*).returns(Right(Seq("src/Coverage/FooBar.cs")))

        (coverageServices.sendReport _)
          .when(*, *, *, *, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
          .returns(FailedResponse("Failed to send report"))

        assertCodacyCoverage(
          coverageServices,
          gitFileFetcher,
          List("src/test/resources/dotcover-example.xml"),
          success = false
        )
      }
    }

    "succeed if it can parse and send the report, able to get git files" in {
      val coverageServices = stub[CoverageServices]
      val gitFileFetcher = stub[GitFileFetcher]

      (gitFileFetcher.forCommit _).when(*).returns(Right(Seq("src/Coverage/FooBar.cs")))

      (coverageServices.sendReport _)
        .when(*, *, *, *, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
        .returns(SuccessfulResponse(RequestSuccess("Success")))

      assertCodacyCoverage(
        coverageServices,
        gitFileFetcher,
        List("src/test/resources/dotcover-example.xml"),
        success = true
      )
    }

    "succeed if it can parse and send the report, unable to get git files" in {
      val coverageServices = stub[CoverageServices]
      val gitFileFetcher = stub[GitFileFetcher]

      (gitFileFetcher.forCommit _).when(*).returns(Left("Unable to fetch Git Files"))

      (coverageServices.sendReport _)
        .when(*, *, *, *, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
        .returns(SuccessfulResponse(RequestSuccess("Success")))

      assertCodacyCoverage(
        coverageServices,
        gitFileFetcher,
        List("src/test/resources/dotcover-example.xml"),
        success = true
      )
    }

    "succeed even if the provider paths and the report paths have different cases" in {
      val coverageServices = stub[CoverageServices]
      val gitFileFetcher = stub[GitFileFetcher]

      (gitFileFetcher.forCommit _)
        .when(*)
        .returns(Left("The report has files with different cases"))

      (coverageServices.sendReport _)
        .when(*, *, *, *, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
        .returns(SuccessfulResponse(RequestSuccess("Success")))

      assertCodacyCoverage(
        coverageServices,
        gitFileFetcher,
        List("src/test/resources/test-paths-with-different-paths.xml"),
        success = true
      )
    }

    "succeed even if one of the parsed reports ends up empty" in {
      val coverageServices = stub[CoverageServices]
      val gitFileFetcher = stub[GitFileFetcher]

      (gitFileFetcher.forCommit _).when(*).returns(Right(Seq("src/Coverage/FooBar.cs")))

      (coverageServices.sendReport _)
        .when(*, *, *, *, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
        .returns(SuccessfulResponse(RequestSuccess("Success")))

      (coverageServices.sendFinalNotification _)
        .when(*, Some(RequestTimeout(1000, 10000)), Some(10000), Some(3))
        .returns(SuccessfulResponse(RequestSuccess("Success")))

      assertCodacyCoverage(
        coverageServices,
        gitFileFetcher,
        List("src/test/resources/dotcover-example.xml", "src/test/resources/non-git-report.xml"),
        success = true
      )
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

      langEither should be(Symbol("right"))
      langEither.value should be(conf.language.get)
    }

    "provide the Scala language from report" in {
      val langEither = components.reportRules
        .guessReportLanguage(languageOpt = None, report = coverageReport, reportFilePath = "I/am/a/file.extension")

      langEither should be(Symbol("right"))
      langEither.value should be(Languages.Scala.name)
    }

    "not provide the language from an empty report" in {
      components.reportRules.guessReportLanguage(
        languageOpt = None,
        report = noLanguageReport,
        reportFilePath = "I/am/a/file.extension"
      ) should be(Symbol("left"))
    }
  }

  "guessReportFiles" should {
    "provide a report file" in {
      val projectFiles = List(new File("jacoco-coverage.xml"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("jacoco-coverage.xml"))
    }

    "not provide a report file" in {
      val projectFiles = List(new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be(Symbol("left"))
    }

    "provide the available report file" in {
      val files = new File("coverage.xml") :: Nil
      val projectFiles = new File("coverage.xml") :: new File("other.xml") :: Nil
      val reportEither = components.reportRules.guessReportFiles(files = files, projectFiles = projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value should be(files)
    }

    "only provide phpunit report file inside coverage-xml" in {
      val projectFiles = List(new File("index.xml"), new File("coverage-xml", "index.xml"))
      val reportEither = components.reportRules.guessReportFiles(List.empty, projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value should be(List(new File("coverage-xml", "index.xml")))
    }

    "find an lcov report" in {
      val projectFiles =
        List(new File("lcov.info"), new File("lcov.dat"), new File("foo.lcov"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(List.empty[File], projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("lcov.info", "lcov.dat", "foo.lcov"))
    }

    "support file globs" in {
      val projectFiles =
        List(new File("foo.lcov"), new File("lcov.info"), new File("bar.lcov"), new File("foobar.txt"))
      val reportEither = components.reportRules.guessReportFiles(new File("*.lcov") :: Nil, projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("foo.lcov", "bar.lcov"))
    }

    "return files untouched when glob doesn't find anything" in {
      val reportEither = components.reportRules.guessReportFiles(new File("unexisting.xml") :: Nil, projectFiles = Nil)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("unexisting.xml"))
    }

    "support file globs with directories" in {
      val projectFiles =
        List(
          new File("target/dir1/coverage.xml"),
          new File("dir2/lcov.info"),
          new File("dir1/bar.lcov"),
          new File("dir2/other.xml")
        )
      val reportEither = components.reportRules.guessReportFiles(new File("**/*.xml") :: Nil, projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("target/dir1/coverage.xml", "dir2/other.xml"))
    }

    "remove duplicates when using file globs" in {
      val projectFiles = List(new File("coverage.xml"))
      val reportEither =
        components.reportRules.guessReportFiles(new File("*.xml") :: new File("coverage.xml") :: Nil, projectFiles)

      reportEither should be(Symbol("right"))
      reportEither.value.map(_.toString) should be(List("coverage.xml"))
    }
  }

  "validateFileAccess" should {
    "not validate file" when {
      "file does not exist" in {
        val file = new File("not-exist.xml")
        val result = components.reportRules.validateFileAccess(file)

        result should be(Symbol("left"))
      }

      "file does not have read access" in {
        val file = File.createTempFile("validateFileAccess", "read-access")
        file.createNewFile()
        file.setReadable(false)
        file.deleteOnExit()

        val result = components.reportRules.validateFileAccess(file)
        result should be(Symbol("left"))
      }
    }

    "validate file" in {
      val file = File.createTempFile("validateFileAccess", "valid")
      file.createNewFile()
      file.deleteOnExit()

      val result = components.reportRules.validateFileAccess(file)
      result should be(Symbol("right"))
    }
  }

  "storeReport" should {
    "not store report" in {
      val emptyReport = CoverageReport(Seq.empty[CoverageFileReport])
      val tempFile = File.createTempFile("storeReport", "not-store")
      val result = components.reportRules.storeReport(emptyReport, tempFile)

      result should be(Symbol("left"))
    }

    "successfully store report" when {
      def storeValidReport() = {
        val emptyReport = CoverageReport(List(CoverageFileReport("file-name", Map.empty)))
        val tempFile = File.createTempFile("storeReport", "not-store")
        components.reportRules.storeReport(emptyReport, tempFile)
      }

      "store is successful" in {
        val result = storeValidReport()
        result should be(Symbol("right"))
      }

      "store report exists" in {
        val result = storeValidReport()
        val resultFile = new File(result.value)
        resultFile should be(Symbol("exists"))
      }
    }
  }
}
