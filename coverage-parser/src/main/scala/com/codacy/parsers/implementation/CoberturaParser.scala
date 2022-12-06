package com.codacy.parsers.implementation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.util.MathUtils._
import com.codacy.parsers.util.TextUtils
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.xml.{Elem, NodeSeq}

object CoberturaParser extends CoverageParser with XmlReportParser {

  override val name: String = "Cobertura"

  private val CoverageTag = "coverage"
  private val LineRateAttribute = "line-rate"

  override def parse(projectRoot: File, reportFile: File): Either[String, CoverageReport] = {
    parseReport(reportFile, s"Could not find top level <$CoverageTag> tag") { node =>
      Right(parseReportNode(projectRoot, node))
    }
  }

  // restricting the schema to <coverage line-rate=...>
  // ensures this will not consider Clover reports which also have a <coverage> tag
  override def validateSchema(xml: Elem): Boolean = (xml \\ CoverageTag \ s"@$LineRateAttribute").nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ CoverageTag

  private def parseReportNode(projectRoot: File, report: NodeSeq) = {
    val projectRootStr: String = TextUtils.sanitiseFilename(projectRoot.getAbsolutePath)

    val total = math.round(TextUtils.asFloat(report \\ CoverageTag \@ LineRateAttribute) * 100)

    val fileReports: List[CoverageFileReport] = (for {
      (filename, classes) <- (report \\ "class").groupBy(c => c \@ "filename")
    } yield {
      val cleanFilename = TextUtils.sanitiseFilename(filename).stripPrefix(projectRootStr).stripPrefix("/")
      lineCoverage(cleanFilename, classes)
    })(collection.breakOut)

    CoverageReport(total, fileReports)
  }

  private def lineCoverage(sourceFilename: String, classes: NodeSeq): CoverageFileReport = {
    val classHit = (classes \\ s"@$LineRateAttribute").map { total =>
      val totalValue = TextUtils.asFloat(total.text)
      math.round(totalValue * 100)
    }
    val fileHit = if (classHit.nonEmpty) { classHit.sum / classHit.length } else 0

    val lineHitMap: Map[Int, Int] =
      (for {
        xClass <- classes
        line <- xClass \\ "line"
      } yield (line \@ "number").toInt -> (line \@ "hits").toIntOrMaxValue).toMap

    CoverageFileReport(sourceFilename, fileHit, lineHitMap)
  }
}
