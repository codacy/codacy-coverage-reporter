package com.codacy.rules.commituuid.providers

import org.scalatest.{EitherValues}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GitHubActionProviderSpec extends AnyWordSpec with Matchers with EitherValues {
  val validPullRequestCommitUuid = "480b8293f4340216b8f630053a230e2867cd5c28"
  val invalidPullRequestCommitUuid = "65e4436280a41c721617cba15f33555d3122907e"

  val validWorkflowRunCommitUuid = "0f33f2554d99ce8c12ab126fc4e06bd2ad663e50"

  "getUUID" should {
    "fail" when {
      "no environment variables are defined" in {
        val provider = GitHubActionProvider
        val commitUuidEither = provider.getValidCommitUUID(Map.empty)
        commitUuidEither should be(Symbol("left"))
      }
      "not pull request and GITHUB_SHA is empty" in {
        val provider = GitHubActionProvider
        val commitUuidEither = provider.getValidCommitUUID(Map("GITHUB_EVENT_NAME" -> "push"))
        commitUuidEither should be(Symbol("left"))
      }
      "github action does not have correct commit sha" in {
        val provider = GitHubActionProvider
        val envVars = Map(
          "GITHUB_EVENT_NAME" -> "pull_request",
          "GITHUB_SHA" -> invalidPullRequestCommitUuid,
          "GITHUB_EVENT_PATH" -> "src/test/resources/invalid-github-action-pull-request-event.json"
        )
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be(Symbol("left"))
      }
    }
    "succeed" when {
      "event is push and GITHUB_SHA has value" in {
        val provider = GitHubActionProvider
        val envVars = Map("GITHUB_EVENT_NAME" -> "push", "GITHUB_SHA" -> validPullRequestCommitUuid)
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be(Symbol("right"))
        commitUuidEither.value.value should be(validPullRequestCommitUuid)
      }
      "event is pull_request and json file includes needed information" in {
        val provider = GitHubActionProvider
        val envVars = Map(
          "GITHUB_EVENT_NAME" -> "pull_request",
          "GITHUB_SHA" -> invalidPullRequestCommitUuid,
          "GITHUB_EVENT_PATH" -> "src/test/resources/github-action-pull-request-event.json"
        )
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be(Symbol("right"))
        commitUuidEither.value.value should be(validPullRequestCommitUuid)
      }
      "event is workflow_run and json file includes needed information" in {
        val provider = GitHubActionProvider
        val envVars = Map(
          "GITHUB_EVENT_NAME" -> "workflow_run",
          "GITHUB_SHA" -> invalidPullRequestCommitUuid,
          "GITHUB_EVENT_PATH" -> "src/test/resources/github-action-workflow-run-event.json"
        )
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be(Symbol("right"))
        commitUuidEither.value.value should be(validWorkflowRunCommitUuid)
      }
    }
  }
}
