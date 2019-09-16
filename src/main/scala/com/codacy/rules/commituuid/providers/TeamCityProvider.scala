package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** TeamCity CI provider */
class TeamCityProvider extends CommitUUIDProvider {
  val name: String = "TeamCity CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("TEAMCITY_VERSION").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] = {
    val message = s"""$defaultErrorMessage
		|TEAMCITY_BUILD_COMMIT or BUILD_VCS_NUMBER is not setted. Add this to your config:
		|  env.TEAMCITY_BUILD_COMMIT = %system.build.vcs.number%
		|""".stripMargin

    withErrorMessage(a.get("TEAMCITY_BUILD_COMMIT") orElse a.get("BUILD_VCS_NUMBER"), message)
  }
}
