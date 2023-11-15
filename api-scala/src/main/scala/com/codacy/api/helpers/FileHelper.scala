package com.codacy.api.helpers

import java.io.{File, PrintWriter}

import play.api.libs.json._

import scala.util.Try

object FileHelper {

  def writeJsonToFile[A](file: File, value: A)(implicit writes: Writes[A]): Boolean = {
    val reportJson = Json.stringify(Json.toJson(value))
    val printWriter = new PrintWriter(file)
    val result = Try(printWriter.write(reportJson)).isSuccess
    printWriter.close()
    result
  }

}
