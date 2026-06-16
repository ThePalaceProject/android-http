package org.librarysimplified.http.api

/**
 * The type of HTTP authorization mechanisms.
 */

interface LSHTTPAuthorizationType {
  /**
   * @return The value as it will appear directly in an HTTP Authorization header
   */

  fun toHeaderValue(): String
}
