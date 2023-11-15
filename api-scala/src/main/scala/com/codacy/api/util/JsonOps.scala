package com.codacy.api.util

import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

object JsonOps {

  def handleConversionFailure(error: Seq[(JsPath, Seq[JsonValidationError])]): String = {
    val jsonError = Json.stringify(JsError.toJson(error.toList))
    s"Json conversion error: $jsonError"
  }
}
