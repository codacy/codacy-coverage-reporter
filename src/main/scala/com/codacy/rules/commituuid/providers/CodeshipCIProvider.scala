package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Codeship CI provider */
class CodeshipCIProvider extends CommitUUIDProvider {
  val name: String = "Codeship CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("CI_NAME").contains("codeship")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("CI_COMMIT_ID"))
}
