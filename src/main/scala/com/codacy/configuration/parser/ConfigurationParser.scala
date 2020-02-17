package com.codacy.configuration.parser

import java.io.File

import caseapp._
import caseapp.core.ArgParser
import com.codacy.configuration.parser.ConfigArgumentParsers._
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
@ProgName(
  s"java -cp codacy-coverage-reporter-assembly-${Option(BaseCommand.getClass.getPackage.getImplementationVersion).getOrElse("dev")}.jar"
)
case class BaseCommand()

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
    prefix: Option[String]
) extends CommandConfiguration {
  val partialValue: Boolean = partial.## > 0
  val forceLanguageValue: Boolean = forceLanguage.## > 0
}

case class BaseCommandConfig(
    @Name("t") @ValueDescription("your project API token")
    projectToken: Option[String],
    @Name("a") @ValueDescription("your api token")
    apiToken: Option[String],
    @Name("u") @ValueDescription("your username")
    username: Option[String],
    @Name("p") @ValueDescription("project name")
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
}
