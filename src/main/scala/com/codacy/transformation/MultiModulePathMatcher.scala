package com.codacy.transformation

import java.io.File
import java.util.NoSuchElementException

import com.codacy.api.CoverageReport

class MultiModulePathMatcher(moduleSources: List[String]) extends Transformation {

  override def execute(report: CoverageReport): CoverageReport = {
    if (moduleSources.isEmpty) return report

    val originalPathMap = collectFiles(moduleSources).flatMap(originalPath)

    val fileReports = report.fileReports.map {
      fileReport =>
        fileReport.copy(filename = originalPathMap(fileReport.filename))
    }

    report.copy(fileReports = fileReports)
  }

  private def collectFiles(moduleSources: List[String]): Map[String, Array[File]] = {
    moduleSources.map(srcPath =>
      srcPath -> recursiveListContent(new File(srcPath)).filter(_.isFile)
    ).toMap
  }

  private def originalPath(entry: (String, Array[File])) = {
    entry._2.map(
      file => (unixPathStyle(file.getPath).replace(unixPathStyle(entry._1 + File.separator), ""), unixPathStyle(file))
    )
  }

  private def unixPathStyle(current: File): String = unixPathStyle(current.getPath)

  private def unixPathStyle(current: String): String = current.replace("\\", "/")

  private def recursiveListContent(f: File): Array[File] = {
    val these = f.listFiles
    if(these == null) throw new NoSuchElementException("folder not found: " + f.getPath)
    these ++ these.filter(_.isDirectory).flatMap(recursiveListContent)
  }

}
