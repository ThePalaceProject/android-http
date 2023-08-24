package org.librarysimplified.http.tests

import org.slf4j.LoggerFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.X509TrustManager

object LSHTTPUnsafeTLS {

  val logger =
    LoggerFactory.getLogger(LSHTTPUnsafeTLS::class.java)

  fun unsafeTrustManager(): X509TrustManager {
    return object : X509TrustManager {
      override fun checkClientTrusted(
        chain: Array<X509Certificate?>?,
        authType: String?,
      ) {
        logger.debug("checkClientTrusted")
      }

      override fun checkServerTrusted(
        chain: Array<X509Certificate?>?,
        authType: String?,
      ) {
        logger.debug("checkServerTrusted")
      }

      override fun getAcceptedIssuers(): Array<X509Certificate?>? =
        arrayOfNulls(0)
    }
  }

  fun unsafeHostnameVerifier(): HostnameVerifier {
    return HostnameVerifier { hostname, _ ->
      logger.debug("verify: {}", hostname)
      true
    }
  }
}
