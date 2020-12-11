package com.codacy.rules.commituuid

import java.io.File

import com.codacy.rules.commituuid.providers._
import com.codacy.model.configuration.CommitUUID
import wvlet.log.LogSupport

import com.codacy.api.helpers.vcs.{CommitInfo, GitClient}
import scala.util.{Failure, Success}

/**
  * Trait for a provider that can get a [[CommitUUID]] from environment variables.
  */
trait CommitUUIDProvider {

  /** Name of the provider */
  val name: String

  /** Default error message */
  protected val commitNotFoundMessage = s"Can't find $name commit UUID in the environment."

  protected val commitNotValidMessage =
    "Commit UUID is not valid. Make sure the commit SHA consists of 40 hexadecimal characters."

  /** Parses `environmentVariable` as a [[CommitUUID]], validates it and returns it, as a [[Right]].
    *
    * If the parsing or validation fail, return an error message, as a [[Left]].
    *
    * @param environmentVariable An option for the value of an environment variable.
    * @return Commit uuid or an error message
    */
  protected def parseEnvironmentVariable(environmentVariable: Option[String]): Either[String, CommitUUID] =
    environmentVariable match {
      case Some(commitUUID) => CommitUUID.fromString(commitUUID)
      case None => Left(commitNotFoundMessage)
    }

  /** Validates if the environment has the expected variables for this provider.
    *
    * @param environment environment variables.
    * @return `true` if all variables are defined, `false` otherwise.
    */
  def validateEnvironment(environment: Map[String, String]): Boolean

  /** Gets a **valid** commit UUID, as [[Right]], from the environment if one exists under the
    * expected name and [[CommitUUID.isValid is valid]].
    * Otherwise, get an error message, as [[Left]], to present to the user.
    *
    * @param environment environment variables.
    * @return Either a **valid** commit UUID or an error message.
    */
  def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID]
}

/**
  * Provides a commit UUID
  *
  * This object provides a commit uuid from various providers
  */
object CommitUUIDProvider extends LogSupport {

  private val providers = List(
    AppveyorProvider,
    AzurePipelinesProvider,
    BitriseCIProvider,
    BuildkiteCIProvider,
    CircleCIProvider,
    CodefreshCIProvider,
    CodeshipCIProvider,
    DockerProvider,
    GitHubActionProvider,
    GitlabProvider,
    GreenhouseCIProvider,
    HerokuCIProvider,
    JenkinsProvider,
    MagnumCIProvider,
    SemaphoreCIProvider,
    ShippableCIProvider,
    SolanoCIProvider,
    TeamCityProvider,
    TravisCIProvider,
    WerckerCIProvider
  )

  /** Try to get a commit UUID from all available providers.
    *
    * @param environment environment variables.
    * @return Either a commit uuid or an error message.
    */
  def getFromAll(environment: Map[String, String]): Either[String, CommitUUID] = {
    getFromEnvironment(environment) match {
      case Left(msg) =>
        logger.info(msg)
        logger.info("Trying to get commit UUID from local Git directory")
        getLatestFromGit()
      case uuid => uuid
    }
  }

  /** Gets the latest commit uuid from the local git directory.
    *
    * @return Either a commit uuid or an error message.
    */
  def getLatestFromGit(): Either[String, CommitUUID] = {
    val currentPath = new File(System.getProperty("user.dir"))
    new GitClient(currentPath).latestCommitInfo match {
      case Failure(e) =>
        Left("Commit UUID not provided and could not retrieve it from local Git directory")
      case Success(CommitInfo(uuid, authorName, authorEmail, date)) =>
        val info =
          s"""Commit UUID not provided, using latest commit of local Git directory:
            |$uuid $authorName <$authorEmail> $date""".stripMargin

        logger.info(info)
        CommitUUID.fromString(uuid)
    }
  }

  /** Get a commit UUID from the first provider that can find a valid commit UUID in `environment`.
    *
    * @param environment environment variables.
    *
    * @see [[providers]] to check the order for which providers are tested.
    */
  def getFromEnvironment(environment: Map[String, String]): Either[String, CommitUUID] = {

    val validUUID = providers.collectFirst {
      case provider if provider.validateEnvironment(environment) && provider.getValidCommitUUID(environment).isRight =>
        val uuid = provider.getValidCommitUUID(environment)
        uuid.foreach(u => logger.info(s"CI/CD provider ${provider.name} found Commit UUID ${u.value}"))
        uuid
    }

    validUUID
      .toRight("Can't find or validate commit UUID from any supported CI/CD provider.")
      .flatMap(identity)
  }
}
