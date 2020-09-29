package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Bitrise CI provider */
object BitriseCIProvider extends CommitUUIDProvider {
  val name: String = "Bitrise CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("BITRISE_IO").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("GIT_CLONE_COMMIT_HASH"))
}
