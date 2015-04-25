package com.codacy

import java.io.File

import com.codacy.api.Language
import com.codacy.api.client.CodacyClient
import com.codacy.api.helpers.FileHelper
import com.codacy.api.service.CoverageServices
import com.codacy.parsers.implementation.CoberturaParser

object CodacyCoverageReporter {

  case class Config(language: String, codacyProjectToken: String, codacyApiBaseUrl: String, debug: Boolean = false)

  private val publicApiBaseUrl = "https://www.codacy.com"

  def main(args: Array[String]) {
    println("Hello, world!")
    /*
        val codacyProjectToken = settingKey[Option[String]]("Your project token.")
        val coberturaFile = settingKey[File]("Path for project Cobertura file.")
        val codacyApiBaseUrl = settingKey[Option[String]]("The base URL for the Codacy API.")

        lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
          codacyCoverage := {
            codacyCoverageCommand(state.value, baseDirectory.value, coberturaFile.value,
              crossTarget.value / "coverage-report" / "codacy-coverage.json",
              codacyProjectToken.value, codacyApiBaseUrl.value)
          },
          codacyProjectToken := None,
          codacyApiBaseUrl := None,
          coberturaFile := crossTarget.value / ("coverage-report" + File.separator + "cobertura.xml")
        )
    */

    val parser = new scopt.OptionParser[Config]("codacy-coverage-reporter") {
      head("codacy-coverage-reporter", "1.0.0")
      opt[String]('l', "language").required().action { (x, c) =>
        c.copy(language = x)
      }.text("foo is an integer property")
      opt[String]('t', "codacyProjectToken").action { (x, c) =>
        c.copy(codacyProjectToken = x)
      }.text("out is a required file property")
      opt[String]("codacyApiBaseUrl").action { (x, c) =>
        c.copy(codacyApiBaseUrl = x)
      }.text("out is a required file property")
      opt[Unit]("debug").hidden().action { (_, c) =>
        c.copy(debug = true)
      }.text("this option is hidden in the usage text")
      note("some notes.\n")
      help("help").text("prints this usage text")
    }
    // parser.parse returns Option[C]
    parser.parse(args, Config("", "", "")) match {
      case Some(config) =>
        codacyCoverage(None, None, new File("."), new File("."), new File("."))
      case None =>
    }
  }

  def codacyCoverage(codacyToken: Option[String], codacyApiBaseUrl: Option[String], rootProjectDir: File, codacyCoverageFile: File, coberturaFile: File) = {
    FileHelper.withTokenAndCommit(codacyToken) {
      case (projectToken, commitUUID) =>

        val reader = new CoberturaParser(Language.Scala, rootProjectDir, coberturaFile)
        val report = reader.generateReport()

        FileHelper.writeJsonToFile(codacyCoverageFile, report)

        val codacyClient = new CodacyClient(getApiBaseUrl(codacyApiBaseUrl), Some(projectToken))
        val coverageServices = new CoverageServices(codacyClient)

        println(s"Uploading coverage data...")

        coverageServices.sendReport(commitUUID, report) match {
          case requestResponse if requestResponse.hasError =>
            Left(requestResponse.message)
          case requestResponse =>
            Right(requestResponse.message)
        }
    } match {
      case Left(error) =>
        println(error)
      case Right(message) =>
        println(s"Coverage data uploaded. $message")
    }
  }

  private def getApiBaseUrl(codacyApiBaseUrl: Option[String]): Option[String] = {
    sys.env.get("CODACY_API_BASE_URL")
      .orElse(codacyApiBaseUrl)
      .orElse(Some(publicApiBaseUrl))
  }

}
