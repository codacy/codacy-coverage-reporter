package com.codacy.parsers.implementation

import com.codacy.parsers.CoverageParser

import java.io.File

import com.codacy.parsers.util.{MathUtils}

//import com.codacy.parsers.util.MathUtils._

import com.codacy.api.{CoverageFileReport, CoverageReport}
import scala.io.Source

import scala.util.{Failure, Success, Try}

//name.go:line.column,line.column numberOfStatements count

// mode: set
// github.com/cnuss/api_server/server.go:47.2,48.16 2 0
// github.com/cnuss/api_server/server.go:52.2,53.16 2 0
// github.com/cnuss/api_server/server.go:57.2,58.16 2 0
// github.com/cnuss/api_server/server.go:62.2,63.16 2 0
// github.com/cnuss/api_server/server.go:67.2,68.16 2 0
// github.com/cnuss/api_server/server.go:72.2,73.16 2 0
// github.com/cnuss/api_server/server.go:77.2,78.16 2 0

object GoParser extends CoverageParser {

  override val name: String = "Go"

  final val MODE = "mode:"

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] = {
    val report = Try(Source.fromFile(reportFile)) match {
      case Success(lines) =>
        Right(lines.getLines)
      case Failure(ex) =>
        Left(s"Can't load report file. ${reportFile.toString}")
    }

    report.flatMap(parseLines(reportFile, _))
  }

  private def parseLines(reportFile: File, lines: Iterator[String]): Either[String, CoverageReport] = {
    val coverageFileReports =
      lines.foldLeft[Either[String, Seq[CoverageFileReport]]](Right(Seq.empty[CoverageFileReport]))((accum, next) => {
        println(accum)
        accum.flatMap {
          //case reports if next startsWith MODE =>
          // reports.headOption match {
          //   case Some(value) =>
          //     val coverage = next.stripPrefix("\n")
          //     if (coverage.length >= 2 && coverage.forall(_ forall Character.isDigit)) {
          //       val coverageValue = coverage.map(_.toIntOrMaxValue)
          //       Right(
          //         value.copy(coverage = value.coverage + (coverageValue(0) -> coverageValue(1))) +: reports.tail
          //       )
          //     } else Left(s"Misformatting of file ${reportFile.toString}")
          //   case _ => Left(s"Fail to parse ${reportFile.toString}")
          // }
          case reports =>
            val res = Right(reports)
            res
        }
      })
    coverageFileReports.map { fileReports =>
      val totalFileReport = fileReports.map { report =>
        val coveredLines = report.coverage.count { case (_, hit) => hit > 0 }
        val totalLines = report.coverage.size
        val fileCoverage =
          MathUtils.computePercentage(coveredLines, totalLines)

        CoverageFileReport(report.filename, fileCoverage, report.coverage)
      }

      val (covered, total) = totalFileReport
        .map { f =>
          (f.coverage.count { case (_, hit) => hit > 0 }, f.coverage.size)
        }
        .foldLeft(0 -> 0) {
          case ((accumCovered, accumTotal), (nextCovered, nextTotal)) =>
            (accumCovered + nextCovered, accumTotal + nextTotal)
        }

      val totalCoverage = MathUtils.computePercentage(covered, total)
      CoverageReport(totalCoverage, totalFileReport)
    }
  }
}
