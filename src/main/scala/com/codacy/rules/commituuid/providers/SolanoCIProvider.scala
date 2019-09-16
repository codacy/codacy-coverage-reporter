package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Solano CI provider */
class SolanoCIProvider extends CommitUUIDProvider {
  val name: String = "Solano CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("TDDIUM").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("TDDIUM_CURRENT_COMMIT"))
}
