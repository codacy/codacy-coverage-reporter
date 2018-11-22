package com.codacy.configuration.parser

import org.scalatest.{FlatSpec, Matchers}

class ConfigArgumentParsersTest extends FlatSpec with Matchers {

  "fileListParser" should "parse comma separated list" in {
    val result = ConfigArgumentParsers.stringListParser.apply(None, "moduleA/src,moduleB/src", mandatory = true)

    assert(result.right.get._2 == List("moduleA/src", "moduleB/src"))
  }

}
