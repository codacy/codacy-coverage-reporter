package com.codacy.parsers.util

object MathUtils {

  def computePercentage(part: Int, total: Int): Int = {
    if (total == 0) 0 else math.round((part.toFloat / total) * 100)
  }

  implicit class ParseIntOps(val s: String) extends AnyVal {
    def toIntOrMaxValue: Int = BigInt(s).toIntOrMaxValue
  }

  implicit class BigIntOps(val bigInt: BigInt) extends AnyVal {

    def toIntOrMaxValue: Int =
      if (bigInt.isValidInt) bigInt.toInt
      else Int.MaxValue
  }
}
