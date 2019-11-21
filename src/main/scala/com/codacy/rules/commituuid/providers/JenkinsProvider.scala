package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Jenkins CI provider */
class JenkinsProvider extends CommitUUIDProvider {
  val name: String = "Jenkins CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("JENKINS_URL").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("GIT_COMMIT"))
}
