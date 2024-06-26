package com.codacy.transformation

import com.codacy.api.{CoverageFileReport, CoverageReport}
import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PathPrefixerSpec extends AnyWordSpec with Matchers {

  val report = CoverageReport(
    Seq(CoverageFileReport("Filename.scala", Map.empty), CoverageFileReport("OtherFile.scala", Map.empty))
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
