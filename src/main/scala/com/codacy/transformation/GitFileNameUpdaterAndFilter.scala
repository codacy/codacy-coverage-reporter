package com.codacy.transformation
import com.codacy.api.CoverageReport
import com.codacy.transformation.FileNameMatcher.getFilenameFromPath
import wvlet.log.LogSupport

class GitFileNameUpdaterAndFilter(acceptableFileNamesMap: Map[String, Seq[String]])
    extends Transformation
    with LogSupport {
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
      .matchAndReturnName(filename, acceptableFileNamesMap.getOrElse(getFilenameFromPath(filename), Seq.empty))

    if (maybeFilename.isEmpty)
      logger
        .warn(s"File: Ignoring $filename for coverage calculation. No matching file found in the repository.")

    maybeFilename
  }
}
