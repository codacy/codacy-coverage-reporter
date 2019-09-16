package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Appveyor provider */
class AppveyorProvider extends CommitUUIDProvider {
  val name: String = "Appveyor CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("True") && a.get("APPVEYOR").contains("True")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("APPVEYOR_REPO_COMMIT"))
}
