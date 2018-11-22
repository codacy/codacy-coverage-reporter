package com.codacy.transformation

import java.io.File

import com.codacy.api.{CoverageFileReport, CoverageReport}
import org.scalatest._

class MultiModulePathMatcherTest extends FlatSpec with Matchers {

  private val moduleA = "src/test/sample/module-a/src/main/scala"
  private val moduleB = "src/test/sample/module-b/src/main/scala"
  private val moduleA_Filename = "package/in/module/a/Filename.scala"
  private val moduleB_OtherFile = "package/in/module/b/OtherFile.scala"

  val report = CoverageReport(83, Seq(
    CoverageFileReport(moduleA_Filename, 24, Map()),
    CoverageFileReport(moduleB_OtherFile, 74, Map())
  ))


  "MultiModulePathMatcher" should "adjust path for all filenames in a report" in {
    val prefixer = new MultiModulePathMatcher(List(moduleA, moduleB))

    val fileReports = prefixer.execute(report).fileReports
    fileReports.map(_.filename) should contain(moduleA + "/" + moduleA_Filename)
    fileReports.map(_.filename) should contain(moduleB + "/" + moduleB_OtherFile)
  }

  it should "do nothing if no modules specified" in {
    val prefixer = new MultiModulePathMatcher(List())

    val fileReports = prefixer.execute(report).fileReports
    fileReports.map(_.filename) should contain(moduleA_Filename)
    fileReports.map(_.filename) should contain(moduleB_OtherFile)
  }

  it should "fail when file not found" in {
    val caught = intercept[NoSuchElementException] {
      new MultiModulePathMatcher(List(moduleA))
        .execute(report)
    }
    assert(caught.getMessage == "key not found: " + moduleB_OtherFile)
  }

  it should "fail when folder not found" in {
    val wrongPath = "missing/module/path"

    val caught = intercept[NoSuchElementException] {
      new MultiModulePathMatcher(List(moduleA, wrongPath))
      .execute(report)
    }
    assert(caught.getMessage == "folder not found: " + osDependent(wrongPath))
  }

  def osDependent(path: String): String = new File(path).getPath
}
