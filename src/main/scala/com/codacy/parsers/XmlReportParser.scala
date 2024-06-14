package com.codacy.parsers

import java.io.File

import com.codacy.api.CoverageReport
import com.codacy.parsers.util.XMLoader

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

trait XmlReportParser {
  def validateSchema(xml: Elem): Boolean

  def getRootNode(xml: Elem): NodeSeq

  def loadXmlReport(reportFile: File, schemaErrorMessage: String): Either[String, NodeSeq] = {
    Try(XMLoader.loadFile(reportFile)) match {
      case Success(xml) if validateSchema(xml) =>
        Right(getRootNode(xml))

      case Success(_) =>
        Left(s"Invalid report. $schemaErrorMessage.")

      case Failure(ex) =>
        Left(s"Unparseable report. ${ex.getMessage}")
    }
  }

  private def getFailedParseMessage(ex: Throwable) =
    s"Failed to parse report with error: ${ex.getMessage}"

  def parseReport(reportFile: File, schemaErrorMessage: String)(
      parseReport: NodeSeq => Either[String, CoverageReport]
  ): Either[String, CoverageReport] = {
    loadXmlReport(reportFile, schemaErrorMessage)
      .flatMap { report =>
        Try(parseReport(report)) match {
          case Success(coverageReport) => coverageReport
          case Failure(ex) => Left(getFailedParseMessage(ex))
        }
      }
  }
}
