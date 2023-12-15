package com.codacy.parsers.implementation

import java.io.File
import com.codacy.api._
import com.codacy.parsers.util.FileNameMatcher
import com.codacy.parsers.{CoverageParser, XmlReportParser}
import wvlet.log.LogSupport

import scala.xml.{Elem, Node, NodeSeq}

private case class LineCoverage(missedInstructions: Int, coveredInstructions: Int)

object JacocoParser extends CoverageParser with XmlReportParser with LogSupport {

  override val name: String = "Jacoco"

  private val ReportTag = "report"

  override def parse(projectRoot: File, reportFile: File, acceptedFiles: Seq[String]): Either[String, CoverageReport] =
    parseReport(reportFile, s"Could not find top level <$ReportTag> tag") {
      parseReportNode(_, acceptedFiles)
    }

  override def validateSchema(xml: Elem): Boolean = getRootNode(xml).nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ ReportTag

  private def parseReportNode(report: NodeSeq, acceptedFiles: Seq[String]): Either[String, CoverageReport] = {
    val filesCoverage = for {
      pkg <- report \\ "package"
      packageName = pkg \@ "name"
      sourceFile <- pkg \\ "sourcefile"
      filename = s"$packageName/${sourceFile \@ "name"}"
      actualName <- FileNameMatcher
        .matchAndReturnName(filename, acceptedFiles)
        .map(Some(_))
        .getOrElse({
          logger.warn(s"File: $filename will be discarded and will not be considered for coverage calculation")
          None
        })
    } yield lineCoverage(actualName, sourceFile)

    Right(CoverageReport(filesCoverage))
  }

  private def lineCoverage(filename: String, fileNode: Node): CoverageFileReport = {
    val lineHitMap: Map[Int, Int] = (fileNode \\ "line")
      .map { line =>
        (line \@ "nr").toInt -> LineCoverage((line \@ "mi").toInt, (line \@ "ci").toInt)
      }
      .collect {
        case (key, lineCoverage) if lineCoverage.missedInstructions + lineCoverage.coveredInstructions > 0 =>
          key -> (if (lineCoverage.coveredInstructions > 0) 1 else 0)
      }(collection.breakOut)

    CoverageFileReport(filename, lineHitMap)
  }
}
