package com.codacy.rules.commituuid.providers

import java.io.File
import java.nio.file.Files

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider
import wvlet.log.LazyLogger

import scala.util.Try

object GitHubActionProvider extends CommitUUIDProvider with LazyLogger {
  val name: String = "GitHub Actions"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("GITHUB_ACTIONS").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] = {
    // If the event is a pull request event the GITHUB_SHA will have a different commit UUID (the id of a merge commit).
    // It happens with all the pull_request* events: pull_request, pull_request_review, pull_request_review_comment and pull_request_target.
    // https://help.github.com/en/actions/reference/events-that-trigger-workflows
    // for this reason, we need to fetch it from the event details that GitHub provides
    // equivalent to doing ${{github.event.pull_request.head.sha}} in a GitHub action workflow
    if (environment.get("GITHUB_EVENT_NAME").exists(_.startsWith("pull_request"))) {
      getPullRequestCommit(environment)
    } else {
      parseEnvironmentVariable(environment.get("GITHUB_SHA"))
    }
  }

  private def getPullRequestCommit(envVars: Map[String, String]): Either[String, CommitUUID] = {
    for {
      eventPath <- envVars.get("GITHUB_EVENT_PATH").toRight("Could not find event description file path")
      eventContent <- readFile(eventPath)
      sha <- extractHeadSHA(eventContent)
      commitUUID <- CommitUUID.fromString(sha)
    } yield commitUUID
  }

  private def readFile(path: String): Either[String, String] = {
    val contentTry = Try {
      val fileBytes = Files.readAllBytes(new File(path).toPath)
      new String(fileBytes)
    }

    contentTry.toEither.left.map { ex =>
      logger.error("Failed to read event file", ex)
      s"Failed to read event file with error: ${ex.getMessage}"
    }
  }

  private def extractHeadSHA(event: String) = {
    val eventJson = ujson.read(event)
    Try(eventJson("pull_request")("head")("sha").str).toEither.left
      .map(t => s"Unable to fetch SHA from event file. Failed with error: ${t.getMessage}")
  }
}
