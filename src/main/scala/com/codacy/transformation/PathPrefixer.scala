package com.codacy.transformation

import com.codacy.api.CoverageReport

class PathPrefixer(prefix: String) extends Transformation {

  override def execute(report: CoverageReport): CoverageReport = {
    val fileReports = report.fileReports.map { fileReport =>
      fileReport.copy(filename = prefix + fileReport.filename)
    }

    report.copy(fileReports = fileReports)
  }

}
