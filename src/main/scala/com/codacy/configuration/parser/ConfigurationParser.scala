package com.codacy.configuration.parser

import java.io.File
import caseapp._
import caseapp.core.ArgParser
import com.codacy.api.OrganizationProvider
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
    @Name("l") @ValueDescription("language associated with your coverage report")
    language: Option[String],
    @Hidden @Name("f")
    forceLanguage: Int @@ Counter = Tag.of(0),
    @Name("r") @ValueDescription("your project coverage file name (supports globs)")
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
    @Name("a") @ValueDescription("your account api token")
    apiToken: Option[String],
    @ValueDescription("organization provider")
    organizationProvider: Option[OrganizationProvider.Value],
    @Name("u") @ValueDescription("your username")
    username: Option[String],
    @Name("p") @ValueDescription("project name")
    projectName: Option[String],
    @ValueDescription("the base URL for the Codacy API")
    codacyApiBaseUrl: Option[String],
    @ValueDescription("your commit SHA-1 hash")
    commitUUID: Option[String],
    @ValueDescription(
      "Sets a specified read timeout value, in milliseconds, to be used when interacting with Codacy API. By default, the value is 10 seconds"
    )
    httpTimeout: Int = 10000,
    @ValueDescription(
      "Sets a specified time, in milliseconds, to be used when waiting between retries. By default, the value is 10 seconds"
    )
    sleepTime: Int = 10000,
    @ValueDescription("Sets a number of retries in case of failure. By default, the value is 3 times")
    numRetries: Int = 3,
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
    "lcov" -> LCOVParser,
    "go" -> GoParser
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

  implicit val organizationProvider: ArgParser[OrganizationProvider.Value] =
    ArgParser.instance("organizationProvider") { v =>
      val value = v.trim.toLowerCase
      OrganizationProvider.values.find(_.toString == value) match {
        case Some(provider) => Right(provider)
        case _ =>
          Left(
            s"${value} is an unsupported/unrecognized organization provider. (Available organization provider are: ${OrganizationProvider.values
              .mkString(",")})"
          )
      }
    }
}
