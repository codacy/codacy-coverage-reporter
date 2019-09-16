package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Buildkite CI provider */
class BuildkiteCIProvider extends CommitUUIDProvider {
  val name: String = "Buildkite CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("BUILDKITE").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("BUILDKITE_COMMIT"))
}
