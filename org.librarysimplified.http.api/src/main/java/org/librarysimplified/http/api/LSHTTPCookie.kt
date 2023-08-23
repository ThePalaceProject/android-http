package org.librarysimplified.http.api

import org.joda.time.LocalDateTime

/**
 * A received HTTP cookie.
 *
 * @see "https://tools.ietf.org/html/rfc2965"
 * @see "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie"
 */

data class LSHTTPCookie(
  val name: String,
  val value: String,
  val attributes: Map<String, String>,
  val secure: Boolean,
  val httpOnly: Boolean,
  val expiresAt: LocalDateTime?,
)
