package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Travis CI provider */
object TravisCIProvider extends CommitUUIDProvider {
  val name: String = "Travis CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.get("TRAVIS").contains("true")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("TRAVIS_PULL_REQUEST_SHA") orElse environment.get("TRAVIS_COMMIT"))
}
