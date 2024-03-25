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
    // if the event is a pull_request or a workflow_run the GITHUB_SHA will
    // have a different commit UUID (the id of a merge commit)
    // https://help.github.com/en/actions/reference/events-that-trigger-workflows
    // for this reason, we need to fetch it from the event details that GitHub provides
    // equivalent to doing ${{github.event.pull_request.head.sha}} in a GitHub action workflow
    environment.get("GITHUB_EVENT_NAME") match {
      case Some(eventName @ ("pull_request" | "workflow_run")) =>
        getEventCommitSha(environment, eventName)
      case _ =>
        parseEnvironmentVariable(environment.get("GITHUB_SHA"))
    }
  }

  private def getEventCommitSha(envVars: Map[String, String], eventName: String): Either[String, CommitUUID] = {
    for {
      eventPath <- envVars.get("GITHUB_EVENT_PATH").toRight("Could not find event description file path")
      eventContent <- readFile(eventPath)
      sha <- extractHeadSHA(eventName = eventName, eventContent = eventContent)
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

  private def extractHeadSHA(eventName: String, eventContent: String) = {
    Try {
      val eventJson = ujson.read(eventContent)
      eventName match {
        case "workflow_run" =>
          eventJson(eventName)("head_sha").str
        case _ =>
          eventJson(eventName)("head")("sha").str
      }
    }.toEither.left.map(t => s"Unable to fetch SHA from event file. Failed with error: ${t.getMessage}")
  }
}
