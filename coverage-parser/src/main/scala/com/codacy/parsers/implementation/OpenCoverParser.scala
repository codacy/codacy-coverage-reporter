package com.codacy.parsers.implementation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.util.TextUtils
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.xml.{Elem, NodeSeq}

object OpenCoverParser extends CoverageParser with XmlReportParser {
  private val RootTag = "CoverageSession"
  private val IdAttribute = "uid"
  private val FileTag = "File"
  private val FileRefTag = "FileRef"
  private val LineAttribute = "sl"
  private val VisitCounterAttribute = "vc"
  private val FilesTag = "Files"
  private val FullPathAttribute = "fullPath"
  private val MethodTag = "Method"
  private val SequencePointTag = "SequencePoint"

  override val name: String = "OpenCover"

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] =
    parseReport(reportFile, s"Could not find tag <$RootTag>") { node =>
      Right(parseReportNode(node, TextUtils.sanitiseFilename(rootProject.getAbsolutePath)))
    }

  override def validateSchema(xml: Elem): Boolean = getRootNode(xml).nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ RootTag

  private def parseReportNode(rootNode: NodeSeq, projectRoot: String): CoverageReport = {
    val fileIndices: Map[Int, String] = (rootNode \\ FilesTag \ FileTag).map { n =>
      (n \@ IdAttribute).toInt -> n \@ FullPathAttribute
    }.toMap

    val validMethods = (rootNode \\ MethodTag).filter(m => (m \ FileRefTag).nonEmpty)

    val fileReports = (for {
      (fileIndex, methods) <- validMethods.groupBy(m => (m \ FileRefTag \@ IdAttribute).toInt)
      filename <- fileIndices.get(fileIndex)

      sanitisedFileName = TextUtils.sanitiseFilename(filename).stripPrefix(projectRoot).stripPrefix("/")
      lineCoverage = getLineCoverage(methods)
    } yield CoverageFileReport(sanitisedFileName, lineCoverage)).toSeq

    CoverageReport(fileReports)
  }

  private def getLineCoverage(methodNodes: NodeSeq) = {
    val lineCoverage = for {
      methodNode <- methodNodes
      sequencePoint <- methodNode \\ SequencePointTag
    } yield (sequencePoint \@ LineAttribute).toInt -> (sequencePoint \@ VisitCounterAttribute).toInt

    lineCoverage.toMap
  }
}
