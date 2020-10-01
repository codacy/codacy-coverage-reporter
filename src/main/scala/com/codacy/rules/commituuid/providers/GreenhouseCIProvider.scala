package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Greenhouse CI provider */
object GreenhouseCIProvider extends CommitUUIDProvider {
  val name: String = "Greenhouse CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("GREENHOUSE").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("GREENHOUSE_COMMIT"))
}
