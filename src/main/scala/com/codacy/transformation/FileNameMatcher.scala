package com.codacy.transformation

import java.nio.file.Paths
import scala.util.Try

object FileNameMatcher {

  def matchAndReturnName(filename: String, fileNames: Seq[String]): Option[String] = {
    fileNames
      .filter(name => isTheSameFile(filename.toLowerCase, name.toLowerCase))
      .sortBy(name => Math.abs(filename.length - name.length))
      .headOption
  }

  def getFilenameFromPath(filename: String): String = {
    Try(Paths.get(filename).getFileName.toString.toLowerCase).getOrElse(filename.toLowerCase)
  }

  private def haveSameName(file: String, covFile: String): Boolean =
    getFilenameFromPath(file) == getFilenameFromPath(covFile)

  private def haveSamePath(file: String, covFile: String): Boolean =
    file == covFile

  private def fileEndsWithReportPath(file: String, covFile: String): Boolean =
    file.endsWith(covFile)

  private def reportEndsWithFilePath(file: String, covFile: String): Boolean =
    covFile.endsWith(file)

  private def isTheSameFile(file: String, covFile: String): Boolean = {

    haveSameName(file, covFile) && (haveSamePath(file, covFile) ||
    fileEndsWithReportPath(file, covFile) ||
    reportEndsWithFilePath(file, covFile))
  }
}
