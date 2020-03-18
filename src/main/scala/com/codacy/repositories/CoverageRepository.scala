package com.codacy.repositories

import com.codacy.api.CoverageReport

trait CoverageRepository {

  def addReport(
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean = false
  ): Either[String, Unit]

  def addReportWithProjectName(
      username: String,
      projectName: String,
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean = false
  ): Either[String, Unit]

  def commitReports(commitUuid: String): Either[String, String]
  def commitReportsWithProjectName(username: String, projectName: String, commitUuid: String): Either[String, String]
}
