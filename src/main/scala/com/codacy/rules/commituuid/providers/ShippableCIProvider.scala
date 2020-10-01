package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Shippable CI provider */
object ShippableCIProvider extends CommitUUIDProvider {
  val name: String = "Shippable CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("SHIPPABLE").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("COMMIT"))
}
