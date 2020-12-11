package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Azure Pipelines provider */
object AzurePipelinesProvider extends CommitUUIDProvider {
  val name: String = "Azure Pipelines"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.get("TF_BUILD").contains("True")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("BUILD_SOURCEVERSION"))
}
