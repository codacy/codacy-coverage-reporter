package com.codacy.transformation

import com.codacy.api.{Language, CoverageFileReport, CoverageReport}
import org.scalatest._

import scala.collection.mutable.Stack

class PathPrefixerSpec extends FlatSpec with Matchers {

  val report = CoverageReport(Language.Scala, 83, Seq(
    CoverageFileReport("Filename.scala", 24, Map()),
    CoverageFileReport("OtherFile.scala", 74, Map())
  ))

  "PathPrefixer" should "prefix all filenames in a report" in {
    val prefixer = new PathPrefixer("folder/")

    prefixer.execute(report).fileReports.foreach {
      fileReport =>
        fileReport.filename should startWith("folder/")
    }
  }

  it should "do nothing if no prefix is specified" in {
    val prefixer = new PathPrefixer("")

    prefixer.execute(report).fileReports.map(_.filename) should contain("Filename.scala")
    prefixer.execute(report).fileReports.map(_.filename) should contain("OtherFile.scala")
  }

}
