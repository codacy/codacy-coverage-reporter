package com.codacy.transformation

import org.scalatest.{Matchers, WordSpec}

class FileNameMatcherSpec extends WordSpec with Matchers {

  "matchAndReturnName" should {
    "return name from closest match" in {
      // ARRANGE
      val filenames = Seq("src/folder/package/file.txt", "src/folder/package/another-package/file.txt")
      val filename = "package/file.txt"

      // ACT
      val newFilename = FileNameMatcher.matchAndReturnName(filename, filenames)

      // ASSERT
      newFilename.isDefined shouldBe true
      newFilename.leftSideValue shouldBe Some("src/folder/package/file.txt")
    }

    "return empty when name doesn't match any filenames" in {
      // ARRANGE
      val filenames = Seq("src/folder/package/file.txt", "src/folder/package/another-package/file.txt")
      val filename = "another-package/non-existent-file.txt"

      // ACT
      val newFilename = FileNameMatcher.matchAndReturnName(filename, filenames)

      // ASSERT
      newFilename.isDefined shouldBe false
    }
  }
}
