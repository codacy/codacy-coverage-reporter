package com.codacy.rules.commituuid.providers

import com.codacy.rules.commituuid.CommitUUIDProvider
import com.codacy.model.configuration.CommitUUID

class GitHubActionProvider extends CommitUUIDProvider {
  val name: String = "GitHub Actions"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("GITHUB_ACTIONS").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("GITHUB_SHA"))
}
