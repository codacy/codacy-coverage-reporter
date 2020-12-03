package com.codacy.parsers.implementation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.util.{MathUtils, TextUtils}
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.xml.{Elem, NodeSeq}

case class StatementNode(fileIndex: Int, line: Int, covered: Boolean)

object DotcoverParser extends CoverageParser with XmlReportParser {
  override val name: String = "DotCover"

  private val RootTag = "Root"
  private val CoverageAttribute = "CoveragePercent"
  private val CoveredAttribute = "Covered"

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] =
    parseReport(reportFile, s"Could not find tag <$RootTag $CoverageAttribute=...>") { node =>
      Right(parseReportNode(rootProject, node))
    }

  override def validateSchema(xml: Elem): Boolean = (xml \\ RootTag \ s"@$CoverageAttribute").nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ RootTag

  private def parseReportNode(rootProject: File, rootNode: NodeSeq): CoverageReport = {
    val projectRootStr: String = TextUtils.sanitiseFilename(rootProject.getAbsolutePath)

    val totalCoverage = (rootNode \@ CoverageAttribute).toInt

    val fileIndices: Map[Int, String] = (rootNode \ "FileIndices" \ "File").map { x =>
      (x \@ "Index").toInt -> (x \@ "Name")
    }.toMap

    val statementsPerFile: Map[Int, NodeSeq] = (rootNode \\ "Statement").groupBy(x => (x \@ "FileIndex").toInt)

    val fileReports = for {
      (fileIndex, statements) <- statementsPerFile
      filename = TextUtils.sanitiseFilename(fileIndices(fileIndex)).stripPrefix(projectRootStr).stripPrefix("/")
      lineCoverage = getLineCoverage(statements)
      totalLines = lineCoverage.keys.size
      coveredLines = lineCoverage.values.count(_ > 0)
      total = MathUtils.computePercentage(coveredLines, totalLines)
    } yield CoverageFileReport(filename, total, lineCoverage)

    CoverageReport(totalCoverage, fileReports.toSeq)
  }

  private def getLineCoverage(statementNodes: NodeSeq) = {
    val lines = for {
      node <- statementNodes
      // a statement can extend over several lines
      line <- (node \@ "Line").toInt to (node \@ "EndLine").toInt
      coveredValue = if ((node \@ CoveredAttribute).toBoolean) 1 else 0
    } yield (line, coveredValue)

    lines.toMap
  }
}
