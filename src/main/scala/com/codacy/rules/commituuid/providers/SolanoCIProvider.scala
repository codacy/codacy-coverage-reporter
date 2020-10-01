package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Solano CI provider */
object SolanoCIProvider extends CommitUUIDProvider {
  val name: String = "Solano CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("TDDIUM").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("TDDIUM_CURRENT_COMMIT"))
}
