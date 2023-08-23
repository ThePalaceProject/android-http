package org.librarysimplified.http.oauth_client_credentials

import org.librarysimplified.http.api.LSHTTPRequestBuilderType
import org.librarysimplified.http.api.LSHTTPRequestProperties
import java.net.URI

private const val AUTHENTICATE_URI_PROPERTY_NAME =
  "org.librarysimplified.http.oauth_client_credentials.authenticateURI"

val LSHTTPRequestProperties.oauthAuthenticateURI: URI?
  get() = otherProperties[AUTHENTICATE_URI_PROPERTY_NAME] as? URI?

fun LSHTTPRequestBuilderType.setOAuthAuthenticateURI(uri: URI) {
  setExtensionProperty(AUTHENTICATE_URI_PROPERTY_NAME, uri)
}
