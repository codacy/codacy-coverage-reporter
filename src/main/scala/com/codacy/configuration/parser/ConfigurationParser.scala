package com.codacy.configuration.parser

import java.io.File

import caseapp._
import caseapp.core.ArgParser
import com.codacy.configuration.parser.ConfigArgumentParsers._

abstract class ConfigurationParsingApp extends CommandAppWithPreCommand[BaseCommand, CommandConfiguration] {
  override final def run(options: CommandConfiguration, remainingArgs: RemainingArgs): Unit = {
    run(options)
  }

  def run(config: CommandConfiguration): Unit

  override def beforeCommand(options: BaseCommand, remainingArgs: Seq[String]): Unit = ()
}


@AppName("codacy-coverage-reporter")
@AppVersion(Option(BaseCommand.getClass.getPackage.getImplementationVersion).getOrElse("dev"))
@ProgName(s"java -cp codacy-coverage-reporter-assembly-${Option(BaseCommand.getClass.getPackage.getImplementationVersion).getOrElse("dev")}.jar")
case class BaseCommand()

sealed trait CommandConfiguration {
  def baseConfig: BaseCommandConfig
}

case class Final(@Recurse
                 baseConfig: BaseCommandConfig
                ) extends CommandConfiguration

case class Report(@Recurse
                  baseConfig: BaseCommandConfig,
                  @Name("l") @ValueDescription("your project language")
                  language: String,
                  @Hidden @Name("f")
                  forceLanguage: Option[Unit],
                  @Name("r") @ValueDescription("your project coverage file name")
                  coverageReport: File,
                  @ValueDescription("the project path prefix")
                  prefix: Option[String]
                 ) extends CommandConfiguration


case class BaseCommandConfig(@Name("t") @ValueDescription("your project API token")
                             projectToken: Option[String],
                             @ValueDescription("the base URL for the Codacy API")
                             codacyApiBaseUrl: Option[String],
                             @ValueDescription("your commitUUID")
                             commitUUID: Option[String],
                             @Hidden
                             debug: Option[Unit]
                            )


object ConfigArgumentParsers {

  implicit val fileParser: ArgParser[File] = ArgParser.instance("file")(a => Right(new File(a)))

  implicit val boolean: ArgParser[Option[Unit]] = ArgParser.flag("flag")(_ => Right(Option(())))

}