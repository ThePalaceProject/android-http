package org.librarysimplified.http.api

import one.irradia.mime.api.MIMEType

/**
 * The properties of an HTTP response.
 */

data class LSHTTPResponseProperties(

  /**
   * The RFC7807 problem report, if one was provided.
   */

  val problemReport: LSHTTPProblemReport?,

  /**
   * The HTTP status code, possibly modified by any [LSHTTPProblemReport] returned.
   */

  val status: Int,

  /**
   * The original HTTP status code.
   */

  val originalStatus: Int,

  /**
   * The server message line (such as "UNAUTHORIZED").
   */

  val message: String,

  /**
   * The parsed server content type.
   */

  val contentType: MIMEType,

  /**
   * The length of the content, if available.
   */

  val contentLength: Long?,

  /**
   * The headers returned
   */

  val headers: Map<String, List<String>>,

  /**
   * The cookies returned
   */

  val cookies: List<LSHTTPCookie>,

  /**
   * The authorization used to complete the request, if any. This may be a Simplified bearer token
   * that was transparently negotiated by the client.
   */

  val authorization: LSHTTPAuthorizationType? = null,
) {

  /**
   * The values for the given header, or the empty list if the header does not exist.
   */

  fun headerValues(name: String): List<String> =
    this.headers[name] ?: listOf()

  /**
   * The first value for the given header, or `null` if the header does not exist.
   */

  fun header(name: String): String? =
    this.headerValues(name).firstOrNull()
}
