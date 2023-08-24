package org.librarysimplified.http.api

/**
 * HTTP bearer token authorization.
 */

object LSHTTPAuthorizationBearerToken {

  private class Token(
    private val text: String,
  ) : LSHTTPAuthorizationType {
    override fun toHeaderValue(): String = this.text
  }

  fun ofToken(
    token: String,
  ): LSHTTPAuthorizationType {
    return Token("Bearer $token")
  }
}
