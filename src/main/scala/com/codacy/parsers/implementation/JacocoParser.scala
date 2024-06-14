package com.codacy.parsers.implementation

import java.io.File
import com.codacy.api._
import com.codacy.parsers.util.TextUtils
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.xml.{Elem, Node, NodeSeq}

private case class LineCoverage(missedInstructions: Int, coveredInstructions: Int)

object JacocoParser extends CoverageParser with XmlReportParser {

  override val name: String = "Jacoco"

  private val ReportTag = "report"

  override def parse(projectRoot: File, reportFile: File): Either[String, CoverageReport] =
    parseReport(reportFile, s"Could not find top level <$ReportTag> tag") {
      parseReportNode(projectRoot, _)
    }

  override def validateSchema(xml: Elem): Boolean = getRootNode(xml).nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ ReportTag

  private def parseReportNode(projectRoot: File, report: NodeSeq): Either[String, CoverageReport] = {
    val projectRootStr: String = TextUtils.sanitiseFilename(projectRoot.getAbsolutePath)
    val filesCoverage = for {
      pkg <- report \\ "package"
      packageName = (pkg \@ "name")
      sourceFile <- pkg \\ "sourcefile"
    } yield {
      val filename =
        TextUtils
          .sanitiseFilename(s"$packageName/${(sourceFile \@ "name")}")
          .stripPrefix(projectRootStr)
          .stripPrefix("/")
      lineCoverage(filename, sourceFile)
    }
    Right(CoverageReport(filesCoverage))
  }

  private def lineCoverage(filename: String, fileNode: Node): CoverageFileReport = {
    val lineHitMap: Map[Int, Int] = (fileNode \\ "line").view
      .map { line =>
        (line \@ "nr").toInt -> LineCoverage((line \@ "mi").toInt, (line \@ "ci").toInt)
      }
      .collect {
        case (key, lineCoverage) if lineCoverage.missedInstructions + lineCoverage.coveredInstructions > 0 =>
          key -> (if (lineCoverage.coveredInstructions > 0) 1 else 0)
      }
      .toMap

    CoverageFileReport(filename, lineHitMap)
  }
}
