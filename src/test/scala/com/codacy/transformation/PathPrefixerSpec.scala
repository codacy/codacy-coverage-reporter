package com.codacy.transformation

import com.codacy.api.{CoverageFileReport, CoverageReport}
import org.scalatest._

class PathPrefixerSpec extends WordSpec with Matchers {

  val report = CoverageReport(
    83,
    Seq(CoverageFileReport("Filename.scala", 24, Map.empty), CoverageFileReport("OtherFile.scala", 74, Map.empty))
  )

  "PathPrefixer" should {
    "prefix all filenames in a report" in {
      val prefixer = new PathPrefixer("folder/")

      prefixer.execute(report).fileReports.foreach { fileReport =>
        fileReport.filename should startWith("folder/")
      }
    }
  }

  it should {
    "do nothing if no prefix is specified" in {
      val prefixer = new PathPrefixer("")

      prefixer.execute(report).fileReports.map(_.filename) should contain("Filename.scala")
      prefixer.execute(report).fileReports.map(_.filename) should contain("OtherFile.scala")
    }
  }

}
