package com.codacy.parsers.util

object MathUtils {

  implicit class ParseIntOps(val s: String) extends AnyVal {
    def toIntOrMaxValue: Int = BigInt(s).toIntOrMaxValue
  }

  implicit class BigIntOps(val bigInt: BigInt) extends AnyVal {

    def toIntOrMaxValue: Int =
      if (bigInt.isValidInt) bigInt.toInt
      else Int.MaxValue
  }
}
