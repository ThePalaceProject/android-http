package org.librarysimplified.http.api

import com.fasterxml.jackson.core.Base64Variants
import java.nio.charset.Charset

/**
 * HTTP Basic authorization.
 */

object LSHTTPAuthorizationBasic {

  private class Basic(
    private val text: String,
  ) : LSHTTPAuthorizationType {
    override fun toHeaderValue(): String = this.text
  }

  fun ofUsernamePassword(
    userName: String,
    password: String,
    encoding: Charset = Charsets.UTF_8,
  ): LSHTTPAuthorizationType {
    val encoded =
      Base64Variants.MIME_NO_LINEFEEDS.encode("$userName:$password".toByteArray(encoding))
    return Basic("Basic $encoded")
  }
}
