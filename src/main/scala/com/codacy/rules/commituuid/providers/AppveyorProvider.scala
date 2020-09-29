package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Appveyor provider */
object AppveyorProvider extends CommitUUIDProvider {
  val name: String = "Appveyor CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("True") && environment.get("APPVEYOR").contains("True")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("APPVEYOR_REPO_COMMIT"))
}
