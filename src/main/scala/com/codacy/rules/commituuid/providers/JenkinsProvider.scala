package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Jenkins CI provider */
object JenkinsProvider extends CommitUUIDProvider {
  val name: String = "Jenkins CI"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("JENKINS_URL")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("GIT_COMMIT"))
}
