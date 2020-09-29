package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Semaphore CI provider */
object SemaphoreCIProvider extends CommitUUIDProvider {
  val name: String = "Semaphore CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("SEMAPHORE").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("REVISION"))
}
