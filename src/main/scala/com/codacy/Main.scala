package com.codacy

import mainargs._

import com.codacy.configuration.parser._
import com.codacy.configuration.parser.ConfigArgumentParsers._

object Main {

  @main
  def report(config: Report): Int = {
    CodacyCoverageReporter.run(config)
  }

  @main
  def `final`(config: Final): Int = {
    CodacyCoverageReporter.run(config)
  }

  def main(args: Array[String]): Unit = {
    sys.exit(ParserForMethods(this).runOrExit(args).asInstanceOf[Int])
  }
}
