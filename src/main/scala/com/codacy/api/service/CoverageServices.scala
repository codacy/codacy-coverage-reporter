package com.codacy.api.service

import com.codacy.api.client.{CodacyClient, Request, RequestResponse, RequestSuccess, RequestTimeout}
import com.codacy.api.{CoverageReport, OrganizationProvider}
import play.api.libs.json.Json

class CoverageServices(client: CodacyClient) {

  /**
    * Send coverage report to Codacy endpoint.
    * This endpoint requires a project token to authenticate the request and identify the project.
    * Therefore, the client must be initialized with a valid project token.
    * @param commitUuid commit unique identifier
    * @param language programing language
    * @param coverageReport coverage report being sent to Codacy
    * @param partial flag that signals if the report operation will be broken in multiple operations
    * @param timeoutOpt socket connection and read timeouts in milliseconds
    * @return Request response
    */
  def sendReport(
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean = false,
      timeoutOpt: Option[RequestTimeout] = None,
      sleepTime: Option[Int] = None,
      numRetries: Option[Int] = None,
  ): RequestResponse[RequestSuccess] = {
    val endpoint = s"coverage/$commitUuid/${encodePathSegment(language.toLowerCase)}"

    postRequest(endpoint, coverageReport, partial, timeoutOpt, sleepTime, numRetries)
  }

  /**
    * Send final notification signaling the end of the report operation.
    * This endpoint requires an account token to authenticate the request and identify the project.
    * Therefore, the client must be initialized with a valid account token.
    * @param commitUuid commit unique identifier
    * @param timeoutOpt socket connection and read timeouts in milliseconds
    * @return Request Response
    */
  def sendFinalNotification(
      commitUuid: String,
      timeoutOpt: Option[RequestTimeout] = None,
      sleepTime: Option[Int] = None,
      numRetries: Option[Int] = None,
  ): RequestResponse[RequestSuccess] = {
    val endpoint = s"commit/$commitUuid/coverageFinal"

    postEmptyRequest(endpoint, timeoutOpt, sleepTime, numRetries)
  }

  /**
    * Send coverage report with a project name to Codacy endpoint.
    * This endpoint requires an account token to authenticate the request.
    * Therefore, the client must be initialized with a valid account token.
    * @param username reporter's username
    * @param projectName name of the project the report pertains
    * @param commitUuid commit unique identifier
    * @param language programing language
    * @param coverageReport coverage report being reported
    * @param partial flag that signals if the report operation will be broken in multiple operations
    * @param timeoutOpt socket connection and read timeouts in milliseconds
    * @return Request Response
    */
  def sendReportWithProjectName(
      provider: OrganizationProvider.Value,
      username: String,
      projectName: String,
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean = false,
      timeoutOpt: Option[RequestTimeout] = None,
      sleepTime: Option[Int] = None,
      numRetries: Option[Int] = None,
  ): RequestResponse[RequestSuccess] = {
    val endpoint =
      s"${provider.toString}/$username/$projectName/commit/$commitUuid/coverage/${encodePathSegment(language.toLowerCase)}"
    postRequest(endpoint, coverageReport, partial, timeoutOpt, sleepTime, numRetries)
  }

  /**
    * Send final notification with a project name, signaling the end of the report operation.
    * This endpoint requires an account token to authenticate the request.
    * Therefore, the client must be initialized with a valid account token.
    * @param username reporter's username
    * @param projectName name of the project the report pertains
    * @param commitUuid commit unique identifier
    * @param timeoutOpt socket connection and read timeouts in milliseconds
    * @return Request Response
    */
  def sendFinalWithProjectName(
      provider: OrganizationProvider.Value,
      username: String,
      projectName: String,
      commitUuid: String,
      timeoutOpt: Option[RequestTimeout] = None,
      sleepTime: Option[Int] = None,
      numRetries: Option[Int] = None
  ): RequestResponse[RequestSuccess] = {
    val endpoint = s"${provider.toString}/$username/$projectName/commit/$commitUuid/coverageFinal"

    postEmptyRequest(endpoint, timeoutOpt, sleepTime, numRetries)
  }

  private def postRequest(
      endpoint: String,
      coverageReport: CoverageReport,
      partial: Boolean,
      timeoutOpt: Option[RequestTimeout],
      sleepTime: Option[Int],
      numRetries: Option[Int]
  ) = {
    val queryParams = getQueryParameters(partial)

    val jsonString = serializeCoverageReport(coverageReport)

    client.post(Request(endpoint, classOf[RequestSuccess], queryParams), jsonString, timeoutOpt, sleepTime, numRetries)
  }

  private def postEmptyRequest(
      endpoint: String,
      timeoutOpt: Option[RequestTimeout],
      sleepTime: Option[Int],
      numRetries: Option[Int]
  ) =
    client.post(Request(endpoint, classOf[RequestSuccess]), "{}", timeoutOpt, sleepTime, numRetries)

  private def getQueryParameters(partial: Boolean) = {
    Map("partial" -> partial.toString)
  }
  private def serializeCoverageReport(coverageReport: CoverageReport) =
    Json.stringify(Json.toJson(coverageReport))

  /**
    * Any encoding that we do here, needs to have the same output
    * of play.utils.UriEncoding.encodePathSegment for our languages.
    * https://github.com/playframework/playframework/blob/316fbd61c9fc6a6081a3aeef7e773c8bbccd0b6b/core/play/src/main/scala/play/utils/UriEncoding.scala#L50
    */
  private def encodePathSegment(segment: String): String = segment.replaceAll(" ", "%20")

}
