package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/* Argo CD Provider */
object DroneCIProvider extends CommitUUIDProvider {
  val name: String = "Drone CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("DRONE").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("DRONE_COMMIT") orElse environment.get("DRONE_COMMIT_SHA"))
}
