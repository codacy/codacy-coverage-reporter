package com.codacy.api.client

import play.api.libs.json.{Json, Reads}

case class RequestSuccess(success: String)

object RequestSuccess {
  implicit val requestSuccessReads: Reads[RequestSuccess] = Json.reads[RequestSuccess]
}
