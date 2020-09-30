package com.codacy.rules.commituuid

import org.scalatest._

class CommitUUIDProviderSpec extends WordSpec with Matchers with EitherValues {
  "getFromEnvironment" should {
    "provide a valid commit uuid" in {
      val envVars =
        Map("JENKINS_URL" -> "https://jenkins.example.com/", "GIT_COMMIT" -> "ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
      val commitUuid = CommitUUIDProvider.getFromEnvironment(envVars)

      commitUuid should be('right)
      commitUuid.right.value.value should be("ad7ce1b9973d31a2794565f892b6ae4cab575d7c")
    }

    "provide the first valid commit uuid, in provider order" in {
      val envVars =
        Map(
          "JENKINS_URL" -> "https://jenkins.example.com/",
          "GIT_COMMIT" -> "ad7ce1b9973d31a2794565f892b6ae4cab575d7c",
          "GITLAB_CI" -> "true",
          "CI_COMMIT_SHA" -> "1b097ecbbdd0204f908087d6fe1b94dc3453eaf9"
        )
      val commitUuid = CommitUUIDProvider.getFromEnvironment(envVars)

      commitUuid should be('right)
      commitUuid.right.value.value should be("1b097ecbbdd0204f908087d6fe1b94dc3453eaf9")
    }

    "not provide a commit uuid if the environment is empty" in {
      val commitUuid = CommitUUIDProvider.getFromEnvironment(Map.empty)
      commitUuid should be('left)
    }

    "not provide a commit uuid if the environment has no valid commits" in {
      val envVars =
        Map("JENKINS_URL" -> "https://jenkins.example.com/", "GIT_COMMIT" -> "Commit UUID")
      val commitUuid = CommitUUIDProvider.getFromEnvironment(envVars)

      commitUuid should be('left)
    }
  }
}
