package com.codacy.repositories
import com.codacy.api.CoverageReport
import com.codacy.api.client.{FailedResponse, RequestResponse, RequestSuccess, SuccessfulResponse}
import com.codacy.api.service.CoverageServices
import com.typesafe.scalalogging.StrictLogging

class CoverageServiceRepository(coverageServices: CoverageServices) extends CoverageRepository with StrictLogging {
  override def addReport(
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean
  ): Either[String, Unit] = {
    val response = coverageServices.sendReport(commitUuid, language, coverageReport, partial)
    convertResponse(response)
  }

  override def addReportWithProjectName(
      username: String,
      projectName: String,
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean
  ): Either[String, Unit] = {
    val response = coverageServices
      .sendReportWithProjectName(username, projectName, commitUuid, language, coverageReport, partial)
    convertResponse(response)
  }

  private def convertResponse(response: RequestResponse[RequestSuccess]) =
    response match {
      case SuccessfulResponse(value) =>
        logger.info(s"Coverage data uploaded. ${value.success}")
        Right(())
      case failed: FailedResponse =>
        val message = handleFailedResponse(failed)
        Left(s"Failed to upload report: $message")
    }

  /**
    * Handle failed response
    *
    * This function handle failed response and transform it to a user readable message.
    * @param response failed response
    * @return user readable message
    */
  private def handleFailedResponse(response: FailedResponse): String = {
    if (response.message.contains("not found")) {
      "Request URL not found. (Check if the token you are using or the API base URL are valid)"
    } else {
      response.message
    }
  }

  override def commitReports(commitUuid: String): Either[String, String] = {
    val response = coverageServices.sendFinalNotification(commitUuid)
    convertFinalResponse(response)
  }

  override def commitReportsWithProjectName(
      username: String,
      projectName: String,
      commitUuid: String
  ): Either[String, String] = {
    val response = coverageServices.sendFinalWithProjectName(username, projectName, commitUuid)
    convertFinalResponse(response)
  }

  private def convertFinalResponse(response: RequestResponse[RequestSuccess]) =
    response match {
      case SuccessfulResponse(value) =>
        Right(s"Final coverage notification sent. ${value.success}")
      case FailedResponse(message) =>
        Left(s"Failed to send final coverage notification: $message")
    }
}
