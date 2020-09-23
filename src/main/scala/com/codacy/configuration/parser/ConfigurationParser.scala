package com.codacy.configuration.parser

import java.io.File

import caseapp._
import caseapp.core.ArgParser
import com.codacy.configuration.parser.ConfigArgumentParsers._
import com.codacy.parsers.CoverageParser
import com.codacy.parsers.implementation._
// Intellij keeps removing this import, I'll leave it here for future reference
// import com.codacy.configuration.parser.ConfigArgumentParsers._

abstract class ConfigurationParsingApp extends CommandAppWithPreCommand[BaseCommand, CommandConfiguration] {
  override final def run(options: CommandConfiguration, remainingArgs: RemainingArgs): Unit = {
    sys.exit(run(options))
  }

  def run(config: CommandConfiguration): Int

  override def beforeCommand(options: BaseCommand, remainingArgs: Seq[String]): Unit = ()
}

@AppName("codacy-coverage-reporter")
@AppVersion(Option(BaseCommand.getClass.getPackage.getImplementationVersion).getOrElse("dev"))
@ProgName(BaseCommand.runToolCommand)
case class BaseCommand()

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

sealed trait CommandConfiguration {
  def baseConfig: BaseCommandConfig
}

case class Final(
    @Recurse
    baseConfig: BaseCommandConfig
) extends CommandConfiguration

case class Report(
    @Recurse
    baseConfig: BaseCommandConfig,
    @Name("l") @ValueDescription("your project language")
    language: Option[String],
    @Hidden @Name("f")
    forceLanguage: Int @@ Counter = Tag.of(0),
    @Name("r") @ValueDescription("your project coverage file name")
    coverageReports: Option[List[File]],
    @ValueDescription("if the report is partial")
    partial: Int @@ Counter = Tag.of(0),
    @ValueDescription("the project path prefix")
    prefix: Option[String],
    @ValueDescription("your coverage parser")
    @HelpMessage(s"Available parsers are: ${ConfigArgumentParsers.parsersMap.keys.mkString(",")}")
    forceCoverageParser: Option[CoverageParser]
) extends CommandConfiguration {
  val partialValue: Boolean = partial.## > 0
  val forceLanguageValue: Boolean = forceLanguage.## > 0
}

case class BaseCommandConfig(
    @Name("t") @ValueDescription("your project API token")
    projectToken: Option[String],
    @Name("a") @ValueDescription("your api token") @Hidden
    apiToken: Option[String],
    @Name("u") @ValueDescription("your username") @Hidden
    username: Option[String],
    @Name("p") @ValueDescription("project name") @Hidden
    projectName: Option[String],
    @ValueDescription("the base URL for the Codacy API")
    codacyApiBaseUrl: Option[String],
    @ValueDescription("your commitUUID")
    commitUUID: Option[String],
    @Name("s") @ValueDescription("skip if token isn't defined")
    skip: Int @@ Counter = Tag.of(0),
    @Hidden
    debug: Int @@ Counter = Tag.of(0)
) {
  val skipValue: Boolean = skip.## > 0
  val debugValue: Boolean = debug.## > 0
}

object ConfigArgumentParsers {

  implicit val fileParser: ArgParser[File] = ArgParser.instance("file")(a => Right(new File(a)))

  val parsersMap = Map(
    "cobertura" -> CoberturaParser,
    "jacoco" -> JacocoParser,
    "clover" -> CloverParser,
    "opencover" -> OpenCoverParser,
    "dotcover" -> DotcoverParser,
    "phpunit" -> PhpUnitXmlParser,
    "lcov" -> LCOVParser
  )

  implicit val coverageParser: ArgParser[CoverageParser] = ArgParser.instance("parser") { v =>
    val value = v.trim.toLowerCase
    parsersMap.get(value) match {
      case Some(parser) => Right(parser)
      case _ =>
        Left(
          s"${value} is an unsupported/unrecognized coverage parser. (Available patterns are: ${parsersMap.keys.mkString(",")})"
        )
    }
  }
}
