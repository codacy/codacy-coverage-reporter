package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Bitrise CI provider */
class BitriseCIProvider extends CommitUUIDProvider {
  val name: String = "Bitrise CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("BITRISE_IO").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("GIT_CLONE_COMMIT_HASH"))
}
