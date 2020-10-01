package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Heroku CI provider */
object HerokuCIProvider extends CommitUUIDProvider {
  val name: String = "Heroku CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CI").contains("true") && environment.contains("HEROKU_TEST_RUN_ID")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("HEROKU_TEST_RUN_COMMIT_VERSION"))
}
