package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Codefresh CI provider */
object CodefreshCIProvider extends CommitUUIDProvider {
  val name: String = "Codefresh CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("CF_BUILD_URL") && environment.contains("CF_BUILD_ID")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("CF_REVISION"))
}
