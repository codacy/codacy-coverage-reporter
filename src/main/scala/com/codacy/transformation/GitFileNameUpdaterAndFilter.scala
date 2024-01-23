package com.codacy.transformation
import com.codacy.api.CoverageReport
import wvlet.log.LogSupport

class GitFileNameUpdaterAndFilter(acceptableFileNames: Seq[String]) extends Transformation with LogSupport {
  override def execute(report: CoverageReport): CoverageReport = {
    val fileReports = for {
      fileReport <- report.fileReports
      fileName <- matchAndReturnName(fileReport.filename)

      newFileReport = fileReport.copy(filename = fileName)
    } yield newFileReport
    report.copy(fileReports = fileReports)
  }

  private def matchAndReturnName(filename: String): Option[String] = {
    val maybeFilename = FileNameMatcher
      .matchAndReturnName(filename, acceptableFileNames)

    if (maybeFilename.isEmpty)
      logger
        .warn(s"File: Ignoring $filename for coverage calculation, no matching file found in the repository.")

    maybeFilename
  }
}
