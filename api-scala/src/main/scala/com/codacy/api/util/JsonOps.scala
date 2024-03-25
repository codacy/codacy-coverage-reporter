package com.codacy.api.util

import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.collection

object JsonOps {

  def handleConversionFailure(error: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): String = {
    val jsonError = Json.stringify(JsError.toJson(error.toList))
    s"Json conversion error: $jsonError"
  }
}
