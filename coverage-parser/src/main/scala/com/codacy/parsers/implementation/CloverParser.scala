package com.codacy.parsers.implementation

import java.io.File
import java.nio.file.Paths

import com.codacy.api.{CoverageFileReport, CoverageReport}
import com.codacy.parsers.util.TextUtils
import com.codacy.parsers.{CoverageParser, XmlReportParser}

import scala.xml.{Elem, Node, NodeSeq}

object CloverParser extends CoverageParser with XmlReportParser {
  private val CoverageTag = "coverage"
  private val ProjectTag = "project"
  private val MetricsTag = "metrics"

  override val name: String = "Clover"

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] = {
    parseReport(reportFile, s"Could not find tag hierarchy <$CoverageTag> <$ProjectTag> <$MetricsTag> tags") { node =>
      parseReportNode(rootProject, node)
    }
  }

  override def validateSchema(xml: Elem): Boolean = (xml \\ CoverageTag \ ProjectTag \ MetricsTag).nonEmpty

  override def getRootNode(xml: Elem): NodeSeq = xml \\ CoverageTag

  private def parseReportNode(rootProject: File, report: NodeSeq): Either[String, CoverageReport] = {
    val rootPath = TextUtils.sanitiseFilename(rootProject.getAbsolutePath)

    val coverageFiles = (report \\ "file").foldLeft[Either[String, Seq[CoverageFileReport]]](Right(List())) {
      case (Right(accomulatedFileReports), fileTag) =>
        val fileReport = getCoverageFileReport(rootPath, fileTag)
        fileReport.right.map(_ +: accomulatedFileReports)

      case (Left(errorMessage), _) => Left(errorMessage)
    }

    coverageFiles.map(CoverageReport(0, _))
  }

  private def getCoverageFileReport(rootPath: String, fileNode: Node): Either[String, CoverageFileReport] = {
    val filePath = getUnixPathAttribute(fileNode, "path")
    val filename = getUnixPathAttribute(fileNode, "name")

    val relativeFilePath = filePath
      .orElse(filename)
      .fold[Either[String, String]] {
        Left("Could not read file path due to missing 'path' and 'name' attributes in the report file tag.")
      } {
        case path if Paths.get(path).isAbsolute =>
          Right(path.stripPrefix(rootPath).stripPrefix("/"))

        case path =>
          Right(path)
      }

    for {
      relativeFilePath <- relativeFilePath
      linesCoverage <- getLinesCoverage(fileNode).left
        .map(errorMessage => s"Could not retrieve lines coverage for file '$relativeFilePath': $errorMessage")
    } yield CoverageFileReport(relativeFilePath, 0, linesCoverage)
  }

  private def getLinesCoverage(fileNode: Node): Either[String, Map[Int, Int]] = {
    val fileLineTags = (fileNode \ "line")

    fileLineTags.foldLeft[Either[String, Map[Int, Int]]](Right(Map.empty[Int, Int])) {
      case (left: Left[_, _], _) => left

      case (Right(lines), line) if (line \@ "type") == "stmt" || (line \@ "type") == "cond" =>
        val lineCoverage = for {
          lineNumber <- getFirstNonEmptyValueAsInt(Seq(line), "num")
          countOfExecutions <- getFirstNonEmptyValueAsInt(Seq(line), "count")
        } yield (lineNumber, countOfExecutions)
        lineCoverage.right.map(lines + _)

      case (accumulated, _) => accumulated
    }
  }

  /* Retrieves the attribute with name @attributeName from @node,
   * converts the contents to string and converts path to unix style
   */
  private def getUnixPathAttribute(node: Node, attributeName: String): Option[String] = {
    node.attribute(attributeName).flatMap(_.headOption.map(_.text)).map(TextUtils.sanitiseFilename)
  }

  /* Retrieves the first non-empty value in attribute with name @attributeName from @nodes
   * and converts the value to a number
   */
  private def getFirstNonEmptyValueAsInt(nodes: Seq[Node], attributeName: String): Either[String, Int] = {
    val attributeValues = nodes
      .flatMap(_.attribute(attributeName).getOrElse(Seq.empty[Node]))

    val firstNonEmptyValue = attributeValues
      .map(_.text.trim)
      .find(_.nonEmpty)

    val result = firstNonEmptyValue.fold[Either[String, String]](
      Left(s"Could not find attribute with name '$attributeName'")
    )(Right(_))

    result.flatMap { attributeValue =>
      parseInt(attributeValue).left.flatMap(_ => Left(s"Value of attribute with name '$attributeName' is not a number"))
    }
  }

  /* Parses string value into a number */
  private def parseInt(value: String): Either[String, Int] = {
    try {
      Right(value.toInt)
    } catch {
      case _: NumberFormatException =>
        Left(s"Value '$value' is not a number")
    }
  }

}
