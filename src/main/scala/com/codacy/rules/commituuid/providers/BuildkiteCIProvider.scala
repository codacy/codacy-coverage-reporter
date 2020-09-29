package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Buildkite CI provider */
object BuildkiteCIProvider extends CommitUUIDProvider {
  val name: String = "Buildkite CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("BUILDKITE").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("BUILDKITE_COMMIT"))
}
