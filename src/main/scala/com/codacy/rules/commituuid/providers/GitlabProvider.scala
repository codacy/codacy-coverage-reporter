package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Gitlab CI provider */
object GitlabProvider extends CommitUUIDProvider {
  val name: String = "Gitlab CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("GITLAB_CI")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("CI_COMMIT_SHA") orElse environment.get("CI_BUILD_REF"))
}
