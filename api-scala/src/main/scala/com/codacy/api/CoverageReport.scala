package com.codacy.api

import play.api.libs.json.{JsNumber, JsObject, Json, Writes}

case class CoverageFileReport(filename: String, coverage: Map[Int, Int])

case class CoverageReport(fileReports: Seq[CoverageFileReport])

object CoverageReport {
  implicit val mapWrites: Writes[Map[Int, Int]] = Writes[Map[Int, Int]] { map: Map[Int, Int] =>
    JsObject(map.map {
      case (key, value) => (key.toString, JsNumber(value))
    })
  }
  implicit val coverageFileReportWrites: Writes[CoverageFileReport] = Json.writes[CoverageFileReport]
  implicit val coverageReportWrites: Writes[CoverageReport] = Json.writes[CoverageReport]
}

object OrganizationProvider extends Enumeration {
  val manual, gh, bb, ghe, bbe, gl, gle = Value
}
