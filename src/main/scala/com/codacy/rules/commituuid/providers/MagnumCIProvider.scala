package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Magnum CI provider */
class MagnumCIProvider extends CommitUUIDProvider {
  val name: String = "Magnum CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("MAGNUM").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("CI_COMMIT"))
}
