package com.codacy.transformation

import com.codacy.api.{CoverageFileReport, CoverageReport}
import org.scalatest.{Matchers, WordSpec}

class GitFileNameUpdaterAndFilterSpec extends WordSpec with Matchers {

  private val acceptableFilenames =
    Seq("src/folder/file1.txt", "src/another-folder/file1.txt", "src/folder/file2.txt", "src/folder/file3.txt")
  private val updaterAndFilter = new GitFileNameUpdaterAndFilter(acceptableFilenames)

  "execute" should {
    "update and match filename" in {
      // ARRANGE
      val expectedFilename = "src/folder/file1.txt"

      val coverageReport = CoverageReport(Seq(CoverageFileReport("folder/file1.txt", Map.empty)))

      // ACT
      val result = updaterAndFilter.execute(coverageReport)

      // ASSERT
      result.fileReports.map(_.filename) shouldBe Seq(expectedFilename)
    }

    "update and match several filenames" in {
      // ARRANGE
      val expectedFilenames =
        Set("src/folder/file1.txt", "src/another-folder/file1.txt", "src/folder/file2.txt", "src/folder/file3.txt")

      val coverageReport = CoverageReport(
        Seq(
          CoverageFileReport("folder/file1.txt", Map.empty),
          CoverageFileReport("another-folder/file1.txt", Map.empty),
          CoverageFileReport("file2.txt", Map.empty),
          CoverageFileReport("src/folder/file3.txt", Map.empty)
        )
      )

      // ACT
      val result = updaterAndFilter.execute(coverageReport)

      // ASSERT
      result.fileReports.map(_.filename).toSet shouldBe expectedFilenames
    }

    "filters out reports not in acceptable filenames" in {
      // ARRANGE
      val coverageReport = CoverageReport(
        Seq(
          CoverageFileReport("folder/file1.txt", Map.empty),
          CoverageFileReport("folder/not-acceptable-file.txt", Map.empty)
        )
      )

      // ACT
      val result = updaterAndFilter.execute(coverageReport)

      // ASSERT
      result.fileReports.size shouldBe 1
    }
  }

}
