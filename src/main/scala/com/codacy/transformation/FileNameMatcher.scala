package com.codacy.transformation

import java.nio.file.Paths
import scala.util.Try
import wvlet.log.LogSupport

object FileNameMatcher extends LogSupport {

  def matchAndReturnName(filename: String, fileNames: Seq[String]): Option[String] = {
    fileNames
      .filter(name => isTheSameFile(filename.toLowerCase, name.toLowerCase))
      .headOption
  }

  def getFilenameFromPath(filename: String): String = {
    Try(Paths.get(filename).getFileName.toString.toLowerCase).getOrElse(filename.toLowerCase)
  }

  private def normalizePath(path: String): String = {
    path.replace("\\", "/")
  }

  private def haveSameName(file: String, covFile: String): Boolean =
    getFilenameFromPath(file) == getFilenameFromPath(covFile)

  private def haveSamePath(file: String, covFile: String): Boolean =
    normalizePath(file) == normalizePath(covFile)

  private def fileEndsWithReportPath(file: String, covFile: String): Boolean =
    normalizePath(file).endsWith(normalizePath(covFile))

  private def reportEndsWithFilePath(file: String, covFile: String): Boolean =
    normalizePath(covFile).endsWith(normalizePath(file))

  private def isTheSameFile(file: String, covFile: String): Boolean = {
    haveSameName(file, covFile) && (haveSamePath(file, covFile) ||
    fileEndsWithReportPath(file, covFile) ||
    reportEndsWithFilePath(file, covFile))
  }
}
