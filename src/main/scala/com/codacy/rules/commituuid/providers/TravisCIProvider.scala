package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Travis CI provider */
class TravisCIProvider extends CommitUUIDProvider {
  val name: String = "Travis CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("TRAVIS").contains("true")
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("TRAVIS_PULL_REQUEST_SHA") orElse a.get("TRAVIS_COMMIT"))
}
