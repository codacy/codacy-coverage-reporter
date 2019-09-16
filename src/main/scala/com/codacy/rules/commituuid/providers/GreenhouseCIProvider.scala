package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Greenhouse CI provider */
class GreenhouseCIProvider extends CommitUUIDProvider {
  val name: String = "Greenhouse CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("GREENHOUSE").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("GREENHOUSE_COMMIT"))
}
