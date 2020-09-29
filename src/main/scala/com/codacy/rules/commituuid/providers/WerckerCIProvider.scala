package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Wercker CI provider */
object WerckerCIProvider extends CommitUUIDProvider {
  val name: String = "Wercker CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.contains("WERCKER_GIT_BRANCH")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("WERCKER_GIT_COMMIT"))
}
