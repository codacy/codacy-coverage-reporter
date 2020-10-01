package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** TeamCity CI provider */
object TeamCityProvider extends CommitUUIDProvider {
  val name: String = "TeamCity CI"

  override val commitNotFoundMessage: String = s"""Can't find $name commit UUID in the environment.
		|TEAMCITY_BUILD_COMMIT or BUILD_VCS_NUMBER is not set. Add this to your config:
		|  env.TEAMCITY_BUILD_COMMIT = %system.build.vcs.number%
		|""".stripMargin

  override def validateEnvironment(environment: Map[String, String]): Boolean = {
    environment.contains("TEAMCITY_VERSION")
  }

  override def getValidCommitUUID(environment: Map[String, String]): Either[String, CommitUUID] = {
    parseEnvironmentVariable(environment.get("TEAMCITY_BUILD_COMMIT") orElse environment.get("BUILD_VCS_NUMBER"))
  }
}
