package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Gitlab CI provider */
class GitlabProvider extends CommitUUIDProvider {
  val name: String = "Gitlab CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("GITLAB_CI").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("CI_COMMIT_SHA") orElse a.get("CI_BUILD_REF"))
}
