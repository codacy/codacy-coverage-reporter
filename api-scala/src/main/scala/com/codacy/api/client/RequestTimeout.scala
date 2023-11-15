package com.codacy.api.client

/**
  * The socket connection and read timeouts in milliseconds.
  */
case class RequestTimeout(connTimeoutMs: Int, readTimeoutMs: Int)
