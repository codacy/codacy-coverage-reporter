package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/* Argo CD Provider */
object ArgoCDProvider extends CommitUUIDProvider {
  val name: String = "Argo CD"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("ARGOCD_APP_SOURCE_REPO_URL")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("ARGOCD_APP_REVISION"))
}
