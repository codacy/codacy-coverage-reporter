package com.codacy.transformation

import java.nio.file.Paths
import scala.util.Try

object FileNameMatcher {

  def matchAndReturnName(filename: String, fileNames: Seq[String]): Option[String] = {
    fileNames
      .filter(name => isTheSameFile(filename, name))
      .sortBy(name => Math.abs(filename.length - name.length))
      .headOption
  }

  def getFilenameFromPath(filename: String): String = {
    Try(Paths.get(filename).getFileName.toString).getOrElse(filename)
  }

  private def haveSameName(file: String, covFile: String): Boolean =
    getFilenameFromPath(file).equalsIgnoreCase(getFilenameFromPath(covFile))

  private def haveSamePath(file: String, covFile: String): Boolean =
    file.toLowerCase.equalsIgnoreCase(covFile.toLowerCase)

  private def fileEndsWithReportPath(file: String, covFile: String): Boolean =
    file.toLowerCase.endsWith(covFile.toLowerCase)

  private def reportEndsWithFilePath(file: String, covFile: String): Boolean =
    covFile.toLowerCase.endsWith(file.toLowerCase)

  private def isTheSameFile(file: String, covFile: String): Boolean = {

    haveSameName(file, covFile) && (haveSamePath(file, covFile) ||
    fileEndsWithReportPath(file, covFile) ||
    reportEndsWithFilePath(file, covFile))
  }
}
