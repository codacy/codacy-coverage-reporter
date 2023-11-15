package com.codacy.parsers.implementation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.util.MathUtils._
import com.codacy.parsers.util.TextUtils
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.collection.mutable
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

    val fileReports: List[CoverageFileReport] = (for {
      (filename, classes) <- (report \\ "class").groupBy(c => c \@ "filename")
    } yield {
      val cleanFilename = TextUtils.sanitiseFilename(filename).stripPrefix(projectRootStr).stripPrefix("/")
      lineCoverage(cleanFilename, classes)
    })(collection.breakOut)

    CoverageReport(fileReports)
  }

  private def lineCoverage(sourceFilename: String, classes: NodeSeq): CoverageFileReport = {
    val map = mutable.Map.empty[Int, Int]

    for {
      xClass <- classes
      line <- xClass \\ "line"
    } {
      val key = (line \@ "number").toInt
      val value = (line \@ "hits").toIntOrMaxValue
      val sum = map.get(key).getOrElse(0) + BigInt(value)

      map(key) = sum.toIntOrMaxValue
    }

    CoverageFileReport(sourceFilename, map.toMap)
  }
}
