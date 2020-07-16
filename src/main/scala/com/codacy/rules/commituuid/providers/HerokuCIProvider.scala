package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Heroku CI provider */
class HerokuCIProvider extends CommitUUIDProvider {
  val name: String = "Heroku CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CI").contains("true") && a.get("HEROKU_TEST_RUN_ID").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("HEROKU_TEST_RUN_COMMIT_VERSION"))
}
