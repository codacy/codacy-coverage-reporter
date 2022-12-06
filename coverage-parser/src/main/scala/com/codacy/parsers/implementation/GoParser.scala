package com.codacy.parsers.implementation

import com.codacy.parsers.CoverageParser

import java.io.File
import com.codacy.parsers.util.MathUtils

import com.codacy.api.{CoverageFileReport, CoverageReport}

import scala.io.Source
import scala.util.{Failure, Success, Try}

case class GoCoverageInfo(filename: String, lineFrom: Int, lineTo: Int, numberOfStatements: Int, countOfStatements: Int)

case class GoCoverageStatementsCount(countStatements: Int, numberStatements: Int, coveredStatements: Int)

case class CoverageFileReportWithStatementsCount(
    statementsCount: GoCoverageStatementsCount,
    coverageFileReport: CoverageFileReport
)

object GoParser extends CoverageParser {

  override val name: String = "Go"

  final val MODE = """mode: ([set|count|atomic]*)""".r

  //filename.go:lineFrom.column,lineTo.column numberOfStatements countOfStatements
  final val regexpString = """([a-zA-Z\/\._\-\d]*):(\d+).*?,(\d+).* (\d+) (\d+)""".r

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] = {
    val report = Try(Source.fromFile(reportFile)) match {
      case Success(lines) =>
        Right(lines.getLines)
      case Failure(ex) =>
        Left("Can't load report file.")
    }

    report.map(lines => parseLines(lines.toList))
  }

  private def parseLines(lines: List[String]): CoverageReport = {
    val coverageInfo = parseAllCoverageInfo(lines)
    val coverageInfoGroupedByFilename: Map[String, Set[GoCoverageInfo]] =
      coverageInfo.toSet.groupBy((a: GoCoverageInfo) => a.filename)

    val coverageFileReports =
      coverageInfoGroupedByFilename.foldLeft[Seq[CoverageFileReportWithStatementsCount]](
        Seq.empty[CoverageFileReportWithStatementsCount]
      )((accum, next) => {
        next match {
          case (filename, coverageInfosForFile) =>
            val statementsCountForFile = coverageInfosForFile.foldLeft(GoCoverageStatementsCount(0, 0, 0)) { (acc, v) =>
              val newCountStatements = acc.countStatements + v.countOfStatements
              val newNumberStatements = acc.numberStatements + v.numberOfStatements
              val newTotalCoveredStatements =
                if (v.countOfStatements > 0) acc.coveredStatements + v.numberOfStatements else acc.coveredStatements

              GoCoverageStatementsCount(newCountStatements, newNumberStatements, newTotalCoveredStatements)
            }

            val coverage = coverageInfosForFile.foldLeft(Map[Int, Int]()) { (acc, coverageInfo) =>
              acc ++ lineHits(coverageInfo)
            }

            val totalForFile = calculateTotal(statementsCountForFile)

            accum :+ CoverageFileReportWithStatementsCount(
              statementsCountForFile,
              CoverageFileReport(filename, totalForFile, coverage)
            )
        }
      })

    val (covered, total) = coverageFileReports
      .foldLeft[(Int, Int)]((0, 0)) {
        case ((covered, total), coverageFileReportWithStatementsCount) =>
          (
            covered + coverageFileReportWithStatementsCount.statementsCount.coveredStatements,
            total + coverageFileReportWithStatementsCount.statementsCount.numberStatements
          )
      }

    val totalCoverage = MathUtils.computePercentage(covered, total)

    CoverageReport(totalCoverage, coverageFileReports.map(_.coverageFileReport))
  }

  private def calculateTotal(coverageFileStatements: GoCoverageStatementsCount): Int = {
    MathUtils.computePercentage(coverageFileStatements.coveredStatements, coverageFileStatements.numberStatements)
  }

  private def lineHits(coverageInfo: GoCoverageInfo): Map[Int, Int] = {
    val lines = Range.inclusive(coverageInfo.lineFrom, coverageInfo.lineTo)
    lines.foldLeft(Map[Int, Int]())((acc, line) => acc ++ Map(line -> coverageInfo.countOfStatements))
  }

  private def parseAllCoverageInfo(lines: List[String]): List[GoCoverageInfo] = {
    lines.collect {
      case regexpString(filename, lineFrom, lineTo, numberOfStatements, countOfStatements, _*) =>
        GoCoverageInfo(filename, lineFrom.toInt, lineTo.toInt, numberOfStatements.toInt, countOfStatements.toInt)
    }
  }

}
