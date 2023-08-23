package org.librarysimplified.http.api

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * The TLS overrides class allows for overriding the various TLS-related classes used in
 * HTTPS. You almost certainly do not want to override the defaults. This class is primarily
 * useful in unit testing where both the client and server are strictly controlled.
 */

data class LSHTTPTLSOverrides(

  /**
   * The factory used to produce SSL sockets.
   */

  val sslSocketFactory: SSLSocketFactory,

  /**
   * The trust manager used to verify client/server certs.
   */

  val trustManager: X509TrustManager,

  /**
   * The verifier used to check hostnames.
   */

  val hostnameVerifier: HostnameVerifier,
)
