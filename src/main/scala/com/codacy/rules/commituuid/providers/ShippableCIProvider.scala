package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Shippable CI provider */
class ShippableCIProvider extends CommitUUIDProvider {
  val name: String = "Shippable CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("SHIPPABLE").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("COMMIT"))
}
