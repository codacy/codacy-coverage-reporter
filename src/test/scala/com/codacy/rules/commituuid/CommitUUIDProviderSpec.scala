package com.codacy.rules.commituuid

import org.scalatest._
import com.codacy.model.configuration.CommitUUID

class CommitUUIDProviderSpec extends WordSpec with Matchers with EitherValues {
  "getFromEnvironment" should {
    "provide a commit uuid" in {
      val envVars = Map("JENKINS_URL" -> "https://jenkins.example.com/", "GIT_COMMIT" -> "abc")
      val commitUuid = CommitUUIDProvider.getFromEnvironment(envVars)

      commitUuid should be('right)
      commitUuid.right.value should be(CommitUUID("abc"))
    }

    "not provide a commit uuid" in {
      val commitUuid = CommitUUIDProvider.getFromEnvironment(Map.empty)

      commitUuid should be('left)
    }
  }
}
