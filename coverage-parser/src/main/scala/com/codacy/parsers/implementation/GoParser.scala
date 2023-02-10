package com.codacy.parsers.implementation

import com.codacy.parsers.CoverageParser

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}

import scala.io.Source
import scala.util.{Failure, Success, Try}

case class GoCoverageInfo(filename: String, lineFrom: Int, lineTo: Int, numberOfStatements: Int, countOfStatements: Int)

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

    val codacyCoverageReport = coverageInfo.map(goCoverage => {
      CoverageFileReport(goCoverage.filename, lineHits(goCoverage))
    })

    CoverageReport(codacyCoverageReport)
  }

  private def lineHits(coverageInfo: GoCoverageInfo): Map[Int, Int] = {
    /*
     | We have a problem that may cause inconsistencies between the report generated by the tool vs our report,
     | go generates a report with coverage values based on statements (NumStmt) and hits (Count)
     | and we calculate based on number of lines ([StartLine,EndLine]) and hist (Count), see code below extracted from Go sdk.
     |
     |// percentCovered returns, as a percentage, the fraction of the statements in
     |// the profile covered by the test run.
     |// In effect, it reports the coverage of a given source file.
     |func percentCovered(p *cover.Profile) float64 {
     |	var total, covered int64
     |	for _, b := range p.Blocks {
     |		total += int64(b.NumStmt)
     |		if b.Count > 0 {
     |			covered += int64(b.NumStmt)
     |		}
     |	}
     |	if total == 0 {
     |		return 0
     |	}
     |	return float64(covered) / float64(total) * 100
     |
     |A go report for a file looks like this
     |    some/fancy/path:42.69,44.16 2 1
     |which can be represented as such
     |
     |    StartLine: 42, StartCol: 69,
     |    EndLine: 44, EndCol: 16,
     |    NumStmt: 2, Count: 1,
     |
     |When they calculate total coverage they do Covered Statements / All the Statements,
     |as we do Covered Lines / Coverable Lines that based on the previous example would look like this:
     |
     |Map( lineNumber, hit) -> (42 -> 1, 43 -> 1, 44 -> 1)
     |
     |We have 3 lines covered as they have 2 "statements covered";
     |(check tests with example Coverage calculated by go coverage tool VS Codacy coverage)
     */
    val lines = Range.inclusive(coverageInfo.lineFrom, coverageInfo.lineTo)
    lines.foldLeft(Map[Int, Int]())((acc, line) => acc ++ Map(line -> coverageInfo.countOfStatements))
  }

  private[parsers] def parseAllCoverageInfo(lines: List[String]): List[GoCoverageInfo] = {
    lines.collect {
      case regexpString(filename, lineFrom, lineTo, numberOfStatements, countOfStatements, _*) =>
        GoCoverageInfo(filename, lineFrom.toInt, lineTo.toInt, numberOfStatements.toInt, countOfStatements.toInt)
    }
  }

}
