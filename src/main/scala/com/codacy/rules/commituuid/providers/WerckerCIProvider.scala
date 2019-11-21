package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Wercker CI provider */
class WerckerCIProvider extends CommitUUIDProvider {
  val name: String = "Wercker CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("WERCKER_GIT_BRANCH").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("WERCKER_GIT_COMMIT"))
}
