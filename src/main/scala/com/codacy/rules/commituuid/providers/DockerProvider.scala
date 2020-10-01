package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Docker provider */
object DockerProvider extends CommitUUIDProvider {
  val name: String = "Docker"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("DOCKER_REPO")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("SOURCE_COMMIT"))
}
