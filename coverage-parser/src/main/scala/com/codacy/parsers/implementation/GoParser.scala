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
    val coverageInfoGroupedByFilename: Map[String, Set[GoCoverageInfo]] =
      coverageInfo.toSet.groupBy((a: GoCoverageInfo) => a.filename)

    val coverageFileReports =
      coverageInfoGroupedByFilename.foldLeft[Seq[CoverageFileReport]](Seq.empty[CoverageFileReport])((accum, next) => {
        next match {
          case (filename, coverageInfosForFile) =>
            //calculate hits for a file for given statement reports
            val coverage = coverageInfosForFile.foldLeft(Map[Int, Int]()) {
              case (hitMapAcc, coverageInfo) =>
                //calculate the range of lines the statement has
                val lines = Range.inclusive(coverageInfo.lineFrom, coverageInfo.lineTo)

                //for each line add the number of hits
                hitMapAcc ++ lines.foldLeft(Map[Int, Int]()) {
                  case (statementHitMapAcc, line) =>
                    statementHitMapAcc ++
                      //if the line is already present on the hit map, don't replace the value
                      Map(line -> (hitMapAcc.getOrElse(line, 0) + coverageInfo.countOfStatements))

                }
            }

            accum :+ CoverageFileReport(filename, coverage)

        }
      })

    CoverageReport(coverageFileReports)
  }

  private def parseAllCoverageInfo(lines: List[String]): List[GoCoverageInfo] = {
    lines.collect {
      case regexpString(filename, lineFrom, lineTo, numberOfStatements, countOfStatements, _*) =>
        GoCoverageInfo(filename, lineFrom.toInt, lineTo.toInt, numberOfStatements.toInt, countOfStatements.toInt)
    }
  }

}
