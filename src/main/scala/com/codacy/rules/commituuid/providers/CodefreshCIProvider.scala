package com.codacy.rules.commituuid.providers

import com.codacy.model.configuration.CommitUUID
import com.codacy.rules.commituuid.CommitUUIDProvider

/** Codefresh CI provider */
class CodefreshCIProvider extends CommitUUIDProvider {
  val name: String = "Codefresh CI"

  override def validate(a: Map[String, String]): Boolean = {
    a.get("CF_BUILD_URL").isDefined && a.get("CF_BUILD_ID").isDefined
  }

  override def getUUID(a: Map[String, String]): Either[String, CommitUUID] =
    withErrorMessage(a.get("CF_REVISION"))
}
