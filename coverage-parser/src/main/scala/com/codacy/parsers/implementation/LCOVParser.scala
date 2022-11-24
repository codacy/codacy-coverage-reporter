package com.codacy.parsers.implementation

import com.codacy.parsers.CoverageParser
import com.codacy.parsers.util.MathUtils._
import com.codacy.api.{CoverageFileReport, CoverageReport}
import java.io.File

import com.codacy.parsers.util.XMLoader

import scala.io.Source
import scala.util.{Failure, Success, Try}

object LCOVParser extends CoverageParser {
  override val name: String = "LCOV"

  final val SF = "SF:"
  final val DA = "DA:"

  override def parse(rootProject: File, reportFile: File): Either[String, CoverageReport] = {
    val report = Try(Source.fromFile(reportFile)) match {
      // most reports are XML, and we want to ensure the LCOV parser won't mishandle it and return an empty result
      case Success(lines) if Try(XMLoader.loadFile(reportFile)).isSuccess =>
        Left(s"The file is not in the lcov format but is an xml.")
      case Success(lines) =>
        Right(lines.getLines)
      case Failure(ex) =>
        Left(s"Can't load report file. ${ex.getMessage}")
    }

    report.flatMap(parseLines(reportFile, _))
  }

  private def parseLines(reportFile: File, lines: Iterator[String]): Either[String, CoverageReport] = {
    val coverageFileReports: Either[String, Seq[CoverageFileReport]] =
      lines.foldLeft[Either[String, Seq[CoverageFileReport]]](Right(Seq.empty[CoverageFileReport]))(
        (accum, next) =>
          accum.flatMap {
            case reports if next startsWith SF =>
              Right(CoverageFileReport(next stripPrefix SF, Map()) +: reports)
            case reports if next startsWith DA =>
              reports.headOption match {
                case Some(value) =>
                  val coverage = next.stripPrefix(DA).split(",")
                  if (coverage.length >= 2 && coverage.forall(_ forall Character.isDigit)) {
                    val coverageValue = coverage.map(_.toIntOrMaxValue)
                    Right(
                      value.copy(coverage = value.coverage + (coverageValue(0) -> coverageValue(1))) +: reports.tail
                    )
                  } else Left(s"Misformatting of file ${reportFile.toString}")
                case _ => Left(s"Fail to parse ${reportFile.toString}")
              }
            case reports =>
              val res = Right(reports)
              res
        }
      )
    coverageFileReports.map { fileReports =>
      val totalFileReport = fileReports.map { report =>
        CoverageFileReport(report.filename, report.coverage)
      }
      CoverageReport(totalFileReport)
    }
  }
}
