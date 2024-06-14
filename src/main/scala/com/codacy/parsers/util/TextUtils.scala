package com.codacy.parsers.util

import java.text.NumberFormat
import java.util.Locale

import scala.util.Try

object TextUtils {

  def asFloat(str: String): Float = {
    Try(str.toFloat).getOrElse {
      // The french locale uses the comma as a sep.
      val instance = NumberFormat.getInstance(Locale.FRANCE)
      val number = instance.parse(str)
      number.floatValue()
    }
  }

  def sanitiseFilename(filename: String): String = {
    filename
      .replaceAll("""\\/""", "/") // Fix for paths with \/
      .replace("\\", "/") // Fix for paths with \
  }

}
