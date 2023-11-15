package com.codacy.api.client

case class Request[T](endpoint: String, classType: Class[T], queryParameters: Map[String, String] = Map.empty)
