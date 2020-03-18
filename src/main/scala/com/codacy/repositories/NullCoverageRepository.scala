package com.codacy.repositories

import com.codacy.api.CoverageReport

class NullCoverageRepository extends CoverageRepository {
  override def addReport(
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean
  ): Either[String, Unit] =
    Right(())

  override def addReportWithProjectName(
      username: String,
      projectName: String,
      commitUuid: String,
      language: String,
      coverageReport: CoverageReport,
      partial: Boolean
  ): Either[String, Unit] =
    Right(())

  override def commitReports(commitUuid: String): Either[String, String] = Right("")

  override def commitReportsWithProjectName(
      username: String,
      projectName: String,
      commitUuid: String
  ): Either[String, String] = Right("")
}
