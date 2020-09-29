package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import org.scalatest.{EitherValues, Matchers, WordSpec}

class GitHubActionProviderSpec extends WordSpec with Matchers with EitherValues {
  val validCommitUuid = "480b8293f4340216b8f630053a230e2867cd5c28"
  val invalidCommitUuid = "65e4436280a41c721617cba15f33555d3122907e"
  "getUUID" should {
    "fail" when {
      "no environment variables are defined" in {
        val provider = GitHubActionProvider
        val commitUuidEither = provider.getValidCommitUUID(Map.empty)
        commitUuidEither should be('left)
      }
      "not pull request and GITHUB_SHA is empty" in {
        val provider = GitHubActionProvider
        val commitUuidEither = provider.getValidCommitUUID(Map("GITHUB_EVENT_NAME" -> "push"))
        commitUuidEither should be('left)
      }
      "github action does not have correct commit sha" in {
        val provider = GitHubActionProvider
        val envVars = Map(
          "GITHUB_EVENT_NAME" -> "pull_request",
          "GITHUB_SHA" -> invalidCommitUuid,
          "GITHUB_EVENT_PATH" -> "src/test/resources/invalid-github-action-event.json"
        )
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be('left)
      }
    }
    "succeed" when {
      "event is push and GITHUB_SHA has value" in {
        val provider = GitHubActionProvider
        val envVars = Map("GITHUB_EVENT_NAME" -> "push", "GITHUB_SHA" -> validCommitUuid)
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be('right)
        commitUuidEither.right.value should be(CommitUUID(validCommitUuid))
      }
      "even is pull request and json file includes needed information" in {
        val provider = GitHubActionProvider
        val envVars = Map(
          "GITHUB_EVENT_NAME" -> "pull_request",
          "GITHUB_SHA" -> invalidCommitUuid,
          "GITHUB_EVENT_PATH" -> "src/test/resources/github-action-event.json"
        )
        val commitUuidEither = provider.getValidCommitUUID(envVars)
        commitUuidEither should be('right)
        commitUuidEither.right.value should be(CommitUUID(validCommitUuid))
      }
    }
  }
}
