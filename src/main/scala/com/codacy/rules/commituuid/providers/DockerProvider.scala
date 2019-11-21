package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Docker provider */
class DockerProvider extends CommitUUIDProvider {
  val name: String = "Docker"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("DOCKER_REPO").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("SOURCE_COMMIT"))
}
