package com.codacy.parsers.implementation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.{CoverageParser, XmlReportParser}
import com.codacy.parsers.util.TextUtils

import scala.xml.{Elem, NodeSeq}

object PhpUnitXmlParser extends CoverageParser with XmlReportParser {
  override val name: String = "PHPUnit"

  private val PhpUnitTag = "phpunit"
  private val ProjectTag = "project"
  private val DirectoryTag = "directory"
  private val XmlParseErrorMessage = s"Could not find top level <$PhpUnitTag> tag";

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] =
    parseReport(reportFile, XmlParseErrorMessage) {
      parseReportNode(rootProject, _, reportFile.getParent)
    }

  override def validateSchema(xml: Elem): Boolean = getRootNode(xml).nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ PhpUnitTag

  private def parseReportNode(
      projectRoot: File,
      report: NodeSeq,
      reportRootPath: String
  ): Either[String, CoverageReport] = {
    val fileNodes = report \ ProjectTag \ DirectoryTag \ "file"
    val projectRootPath = TextUtils.sanitiseFilename(projectRoot.getAbsolutePath)
    val codeDirectory = report \ ProjectTag \ DirectoryTag \@ "name"
    val fileReports = makeFileReports(fileNodes, projectRootPath, codeDirectory, reportRootPath)
    fileReports.map(CoverageReport(_))
  }

  private def makeFileReports(
      fileNodes: NodeSeq,
      projectRootPath: String,
      codeDirectory: String,
      reportRootPath: String
  ): Either[String, Seq[CoverageFileReport]] = {
    val builder = Seq.newBuilder[CoverageFileReport]
    var error = Option.empty[String]
    for (f <- fileNodes if error.isEmpty) {
      val reportFileName = f \@ "href"
      val fileName = getSourceFileName(projectRootPath, codeDirectory, reportFileName)
      getLineCoverage(reportRootPath, reportFileName) match {
        case Right(lineCoverage) =>
          builder += CoverageFileReport(fileName, lineCoverage)
        case Left(message) => error = Some(message)
      }
    }
    error match {
      case Some(value) =>
        Left(value)
      case None =>
        Right(builder.result())
    }
  }

  private def getLineCoverage(reportRootPath: String, filename: String) = {
    val coverageDetailFile = new File(reportRootPath, filename)
    val phpUnitNode = loadXmlReport(coverageDetailFile, XmlParseErrorMessage)

    val lineCoverage: Either[String, Map[Int, Int]] = phpUnitNode.map { node =>
      (node \\ "coverage" \\ "line").map { line =>
        (line \@ "nr").toInt -> (line \ "covered").length
      }.toMap
    }
    lineCoverage
  }

  private def getSourceFileName(pathToRemove: String, codeRootDirectory: String, reportRelativePath: String) = {
    new File(codeRootDirectory, reportRelativePath).getAbsolutePath
      .stripPrefix(pathToRemove)
      .stripPrefix("/")
      .stripSuffix(".xml")
  }
}
