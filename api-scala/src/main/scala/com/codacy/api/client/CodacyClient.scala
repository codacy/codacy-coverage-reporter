package com.codacy.api.client

import com.codacy.api.util.JsonOps
import play.api.libs.json._
import sttp.client3.{HttpURLConnectionBackend, SttpBackendOptions}
import sttp.client3.quick._

import java.net.URL
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class CodacyClient(
    apiUrl: Option[String] = None,
    apiToken: Option[String] = None,
    projectToken: Option[String] = None
) {

  private case class ErrorJson(error: String)
  private case class PaginatedResult[T](next: Option[String], values: Seq[T])

  private implicit val errorJsonFormat: Reads[ErrorJson] = Json.reads[ErrorJson]

  private val tokens = Map.empty[String, String] ++
    apiToken.map(t => "api-token" -> t) ++
    projectToken.map(t => "project-token" -> t) ++
    // This is deprecated and is kept for backward compatibility. It will removed in the context of CY-1272
    apiToken.map(t => "api_token" -> t) ++
    projectToken.map(t => "project_token" -> t)

  private val remoteUrl = new URL(new URL(apiUrl.getOrElse("https://api.codacy.com")), "/2.0").toString()

  /*
   * Does an API post
   */
  def post[T](
      request: Request[T],
      value: String,
      timeoutOpt: Option[RequestTimeout] = None,
      sleepTime: Option[Int],
      numRetries: Option[Int]
  )(implicit reads: Reads[T]): RequestResponse[T] = {
    try {
      var req = quickRequest
        .post(uri"$remoteUrl/${request.endpoint}".withParams(request.queryParameters))
        .headers(tokens ++ Map("Content-Type" -> "application/json"))
        .body(value)

      var options = SttpBackendOptions.Default

      timeoutOpt.foreach { timeout =>
        options = options.connectionTimeout(timeout.connTimeoutMs.millis)
        req = req.readTimeout(timeout.readTimeoutMs.millis)
      }

      val client = simpleHttpClient.withBackend(HttpURLConnectionBackend(options = options))

      val response = client.send(req)

      parseJsonAs[T](response.body) match {
        case failure: FailedResponse =>
          retryPost(request, value, timeoutOpt, sleepTime, numRetries.map(x => x - 1), failure.message)
        case success => success
      }
    } catch {
      case NonFatal(ex) => retryPost(request, value, timeoutOpt, sleepTime, numRetries.map(x => x - 1), ex.getMessage)
    }
  }

  private def retryPost[T](
      request: Request[T],
      value: String,
      timeoutOpt: Option[RequestTimeout],
      sleepTime: Option[Int],
      numRetries: Option[Int],
      failureMessage: String
  )(implicit reads: Reads[T]): RequestResponse[T] = {
    if (numRetries.exists(x => x > 0)) {
      sleepTime.map(x => Thread.sleep(x))
      post(request, value, timeoutOpt, sleepTime, numRetries.map(x => x - 1))
    } else {
      RequestResponse.failure(
        s"Error doing a post to $remoteUrl/${request.endpoint}: exhausted retries due to $failureMessage"
      )
    }
  }

  private def parseJsonAs[T](input: String)(implicit reads: Reads[T]): RequestResponse[T] = {
    parseJson(input) match {
      case failure: FailedResponse => failure
      case SuccessfulResponse(json) =>
        json
          .validate[T]
          .fold(
            errors => FailedResponse(JsonOps.handleConversionFailure(errors)),
            converted => SuccessfulResponse(converted)
          )
    }
  }

  private def parseJson(input: String): RequestResponse[JsValue] = {
    Try(Json.parse(input)) match {
      case Success(json) =>
        json
          .validate[ErrorJson]
          .fold(_ => SuccessfulResponse(json), apiError => FailedResponse(s"API Error: ${apiError.error}"))
      case Failure(exception) =>
        FailedResponse(s"Failed to parse API response as JSON: $input\nUnderlying exception - ${exception.getMessage}")
    }
  }
}
