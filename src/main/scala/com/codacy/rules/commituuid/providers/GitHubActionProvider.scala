package com.codacy.rules.commituuid.providers

import java.nio.file.Files
import java.io.{File, IOException}

import com.codacy.rules.commituuid.CommitUUIDProvider
import com.codacy.model.configuration.CommitUUID
import wvlet.log.LazyLogger

import scala.util.Try

class GitHubActionProvider extends CommitUUIDProvider with LazyLogger {
  val name: String = "GitHub Actions"

  override def validate(envVars: Map[String, String]): Boolean = {
    envVars.get("CI").contains("true") && envVars.get("GITHUB_ACTIONS").contains("true")
  }

  override def getUUID(envVars: Map[String, String]): Either[String, CommitUUID] = {
    // if the event is a pull request the GITHUB_SHA will have a different commit UUID (the id of a merge commit)
    // https://help.github.com/en/actions/reference/events-that-trigger-workflows
    // for this reason, we need to fetch it from the event details that GitHub provides
    // equivalent to doing ${{github.event.pull_request.head.sha}} in a GitHub action workflow
    if (envVars.get("GITHUB_EVENT_NAME").contains("pull_request")) {
      getPullRequestCommit(envVars)
    } else {
      withErrorMessage(envVars.get("GITHUB_SHA"))
    }
  }

  private def getPullRequestCommit(envVars: Map[String, String]): Either[String, CommitUUID] = {
    for {
      eventPath <- envVars.get("GITHUB_EVENT_PATH").toRight("Could not find event description file path")
      eventContent <- readFile(eventPath)
      sha <- extractHeadSHA(eventContent)
    } yield CommitUUID(sha)
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
