package com.codacy.rules.commituuid

import java.io.File

import com.codacy.rules.commituuid.providers._
import com.codacy.model.configuration.CommitUUID
import com.typesafe.scalalogging.StrictLogging

import com.codacy.api.helpers.vcs.{CommitInfo, GitClient}
import scala.util.{Failure, Success}

/**
  * Commit UUID provider
  *
  * This trait is used to implement a provider of a commit UUID.
  */
trait CommitUUIDProvider {

  /** Name of the provider */
  val name: String

  /** Default error message */
  val defaultErrorMessage = s"Can't find $name commit SHA."

  /** Get with error message
    *
    * This function gets an option and transform to an either with an error
    * message.
    *
    * @param opt commit uuid
    * @param errorMessage error message
    * @return commit uuid or an error message
    */
  def withErrorMessage(opt: Option[String], errorMessage: String = defaultErrorMessage): Either[String, CommitUUID] =
    opt match {
      case Some(c) => Right(CommitUUID(c))
      case None => Left(errorMessage)
    }

  /** Validate provider
    *
    * This function validate if this is the selected provider.
    *
    * @param envVars environment variables
    * @return true if its valid, false otherwise
    */
  def validate(envVars: Map[String, String]): Boolean

  /** Get commit UUID
    *
    * This function try to get the commit uuid.
    *
    * @param envVars environment variables
    * @return commit uuid or an error message
    */
  def getUUID(envVars: Map[String, String]): Either[String, CommitUUID]
}

/**
  * Provides a commit UUID
  *
  * This object provides a commit uuid from various providers
  */
object CommitUUIDProvider extends StrictLogging {

  /** Get from all providers
    *
    * This function is to get a commit uuid from all providers available
    * @param environmentVars environment variables
    * @return commit uuid or an error message
    */
  def getFromAll(environmentVars: Map[String, String]): Either[String, CommitUUID] = {
    getFromEnvironment(environmentVars) match {
      case Left(msg) =>
        logger.info(msg)
        logger.info("Trying to get commit from git")
        getFromGit()
      case uuid => uuid
    }
  }

  /** Get from git
    *
    * This function get a commit uuid from git
    *
    * @return commit uuid or an error message
    */
  def getFromGit(): Either[String, CommitUUID] = {
    val currentPath = new File(System.getProperty("user.dir"))
    new GitClient(currentPath).latestCommitInfo match {
      case Failure(e) =>
        Left("Commit UUID not provided and could not retrieve it from current directory")
      case Success(CommitInfo(uuid, authorName, authorEmail, date)) =>
        val info =
          s"""Commit UUID not provided, using latest commit of current directory:
            |$uuid $authorName <$authorEmail> $date""".stripMargin

        logger.info(info)
        Right(CommitUUID(uuid))
    }
  }

  /** Get from environment variables
    *
    * This function gets a commit uuid using environment variables
    *
    * @param environmentVars environment variables
    * @return commit uuid or an error message
    */
  def getFromEnvironment(environmentVars: Map[String, String]): Either[String, CommitUUID] = {
    val providersList = List(
      new AppveyorProvider,
      new BitriseCIProvider,
      new BuildkiteCIProvider,
      new CircleCIProvider,
      new CodefreshCIProvider,
      new CodeshipCIProvider,
      new DockerProvider,
      new GitlabProvider,
      new GreenhouseCIProvider,
      new JenkinsProvider,
      new MagnumCIProvider,
      new SemaphoreCIProvider,
      new ShippableCIProvider,
      new SolanoCIProvider,
      new TeamCityProvider,
      new TravisCIProvider,
      new WerckerCIProvider
    )

    val validUUID = providersList.collectFirst {
      case provider if provider.validate(environmentVars) =>
        logger.trace(s"Using ${provider.name}")
        provider.getUUID(environmentVars)
    }

    validUUID
      .toRight("Can't find any provider.")
      .flatMap(identity)
  }
}
