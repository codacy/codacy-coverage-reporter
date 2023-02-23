package com.codacy.parsers.util

import scala.xml.factory.XMLLoader
import javax.xml.parsers.SAXParserFactory
import scala.xml.{Elem, SAXParser}

object XMLoader extends XMLLoader[Elem] {

  override def parser: SAXParser = {
    val f = SAXParserFactory.newInstance()
    f.setNamespaceAware(false)
    f.setValidating(false)
    f.setFeature("http://xml.org/sax/features/namespaces", false)
    f.setFeature("http://xml.org/sax/features/validation", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    f.newSAXParser()
  }

}
