package org.librarysimplified.http.api

import org.librarysimplified.http.api.LSHTTPRequestBuilderType.Method
import java.net.URI
import java.util.SortedMap

/**
 * The properties of a request.
 */

data class LSHTTPRequestProperties(
  val target: URI,
  val cookies: SortedMap<String, String>,
  val headers: SortedMap<String, String>,
  val method: Method,
  val authorization: LSHTTPAuthorizationType?,
  val otherProperties: Map<String, Any>,
)
