package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Gitlab CI provider */
object BitbucketCloudProvider extends CommitUUIDProvider {
  val name: String = "Bitbucket Cloud Pipeline"

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    // CI is a bit generic and could be used by other CI Providers as well
    // Check on Bitbucket Build Number as well
    environment.contains("CI") and environment.contains("BITBUCKET_BUILD_NUMBER")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] =
    parseEnvironmentVariable(environment.get("BITBUCKET_COMMIT"))
}
