package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** AWS CodeBuild provider */
object AWSCodeBuildProvider extends CommitUUIDProvider {
  val name: String = "AWS CodeBuild"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("CODEBUILD_BUILD_ID").isDefined
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("CODEBUILD_RESOLVED_SOURCE_VERSION"))
}
