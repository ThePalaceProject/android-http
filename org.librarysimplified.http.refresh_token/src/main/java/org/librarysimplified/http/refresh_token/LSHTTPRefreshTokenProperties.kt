package org.librarysimplified.http.refresh_token

import org.librarysimplified.http.api.LSHTTPRequestBuilderType
import org.librarysimplified.http.api.LSHTTPRequestConstants
import org.librarysimplified.http.api.LSHTTPResponseStatus
import java.net.URI

/**
 * The properties required to transparently negotiate a refresh token.
 */

data class LSHTTPRefreshTokenProperties(
  val userName: String,
  val password: String,
  val refreshURI: URI,
) {
  companion object {

    /**
     * Add the necessary extension properties to the given request builder in order to support
     * the transparent negotiation of tokens.
     */

    fun addToRequestBuilder(
      builder: LSHTTPRequestBuilderType,
      properties: LSHTTPRefreshTokenProperties,
    ): LSHTTPRequestBuilderType {
      builder.setExtensionProperty(
        LSHTTPRequestConstants.PROPERTY_KEY_USERNAME,
        properties.userName,
      )
      builder.setExtensionProperty(
        LSHTTPRequestConstants.PROPERTY_KEY_PASSWORD,
        properties.password,
      )
      builder.setExtensionProperty(
        LSHTTPRequestConstants.PROPERTY_KEY_AUTHENTICATION_URL,
        properties.refreshURI.toString(),
      )
      return builder
    }

    /**
     * Retrieve the access token from the given HTTP response if one is present.
     */

    fun accessTokenOf(
      status: LSHTTPResponseStatus,
    ): LSHTTPRefreshAccessToken? {
      val properties = status.properties
      if (properties != null) {
        val header = properties.header(LSHTTPRequestConstants.PROPERTY_KEY_ACCESS_TOKEN)
        if (header != null) {
          return LSHTTPRefreshAccessToken(header)
        }
      }
      return null
    }
  }
}
