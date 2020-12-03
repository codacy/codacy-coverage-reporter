package com.codacy.parsers.util

object MathUtils {

  def computePercentage(part: Int, total: Int): Int = {
    if (total == 0) 0 else math.round((part.toFloat / total) * 100)
  }

  implicit class ParseIntOps(val s: String) extends AnyVal {

    def toIntOrMaxValue: Int = {
      val long = s.toLong
      if (long > Int.MaxValue) Int.MaxValue
      else long.toInt
    }
  }
}
