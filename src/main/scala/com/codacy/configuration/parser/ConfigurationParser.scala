package com.codacy.configuration.parser

import java.io.File

import mainargs._
import com.codacy.parsers.CoverageParser
import com.codacy.parsers.implementation._

object BaseCommand {

  private def runningOnNativeImage: Boolean = {
    val graalVMFlag = Option(System.getProperty("org.graalvm.nativeimage.kind"))
    graalVMFlag.map(p => p == "executable" || p == "shared").getOrElse(false)
  }

  def runToolCommand = {
    if (runningOnNativeImage) {
      "codacy-coverage-reporter"
    } else {
      s"java -jar codacy-coverage-reporter-assembly-${Option(BaseCommand.getClass.getPackage.getImplementationVersion)
        .getOrElse("dev")}.jar"
    }
  }
}

@main
sealed trait CommandConfiguration {
  def baseConfig: BaseCommandConfig
}

@main
case class Final(baseConfig: BaseCommandConfig) extends CommandConfiguration

@main
case class Report(
    baseConfig: BaseCommandConfig,
    @arg(short = 'l', doc = "your project language")
    language: Option[String],
    @arg(short = 'f')
    forceLanguage: Boolean = false,
    @arg(name = "coverage-reports", short = 'r', doc = "your project coverage file name")
    coverageReports: Option[List[File]],
    @arg(doc = "if the report is partial")
    partial: Boolean = false,
    @arg(doc = "the project path prefix")
    prefix: Option[String],
    @arg(name = "force-coverage-parser", doc = "your coverage parser")
    forceCoverageParser: Option[CoverageParser]
) extends CommandConfiguration

@main
case class BaseCommandConfig(
    @arg(name = "project-token", short = 't', doc = "your project API token")
    projectToken: Option[String],
    @arg(name = "api-token", short = 'a', doc = "your api token")
    apiToken: Option[String],
    @arg(short = 'u', doc = "your username")
    username: Option[String],
    @arg(name = "project-name", short = 'p', doc = "project name")
    projectName: Option[String],
    @arg(name = "codacy-api-base-url", doc = "the base URL for the Codacy API")
    codacyApiBaseUrl: Option[String],
    @arg(name = "commit-uuid", doc = "your commitUUID")
    commitUUID: Option[String],
    @arg(short = 's', doc = "skip if token isn't defined")
    skip: Boolean = false,
    @arg
    debug: Boolean = false
)

object ConfigArgumentParsers {

  val parsersMap = Map(
    "cobertura" -> CoberturaParser,
    "jacoco" -> JacocoParser,
    "clover" -> CloverParser,
    "opencover" -> OpenCoverParser,
    "dotcover" -> DotcoverParser,
    "phpunit" -> PhpUnitXmlParser,
    "lcov" -> LCOVParser
  )

  implicit val CoverageParserTokensReader = new TokensReader[CoverageParser](
    "parser",
    strings => {
      val value = strings.head.trim.toLowerCase

      parsersMap.get(value) match {
        case Some(parser) => Right(parser)
        case _ =>
          Left(
            s"${value} is an unsupported/unrecognized coverage parser. (Available patterns are: ${parsersMap.keys.mkString(",")})"
          )
      }
    }
  )
  implicit val fileTokensReader = new TokensReader[java.io.File]("file", a => Right(new File(a.head)))
  implicit val baseCommandConfigParser = ParserForClass[BaseCommandConfig]
  implicit val reportParser = ParserForClass[Report]
  implicit val finalParser = ParserForClass[Final]
}
