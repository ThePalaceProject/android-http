package org.librarysimplified.http.vanilla.internal

import okhttp3.Interceptor
import okhttp3.Response
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.slf4j.LoggerFactory
import java.util.SortedMap

class LSHTTPRedirectRequestInterceptor(
  private val modifier: ((LSHTTPRequestProperties) -> LSHTTPRequestProperties)?,
) : Interceptor {

  private val logger =
    LoggerFactory.getLogger(LSHTTPRedirectRequestInterceptor::class.java)

  override fun intercept(
    chain: Interceptor.Chain,
  ): Response {
    val request =
      chain.request()

    return if (this.modifier != null) {
      val properties =
        request.tag(LSHTTPRequestProperties::class.java)!!
      val adjProperties =
        properties.copy(target = request.url.toUri())
      var newProperties = this.modifier.invoke(adjProperties)

      if (properties.target.host != newProperties.target.host) {
        newProperties = this.dropDangerousRedirectHeaders(newProperties)
      }

      val requestBuilder =
        request.newBuilder()
      val newRequest =
        LSOKHTTPRequests.createRequestForBuilder(newProperties, requestBuilder)

      chain.proceed(newRequest)
    } else {
      chain.proceed(request)
    }
  }

  private fun dropDangerousRedirectHeaders(
    properties: LSHTTPRequestProperties,
  ): LSHTTPRequestProperties {
    var result = properties
    if (result.cookies.isNotEmpty()) {
      this.logger.warn("Dropping {} cookies when redirecting.", result.cookies.size)
      result = result.copy(cookies = sortedMapOf())
    }
    if (result.authorization != null) {
      this.logger.warn("Dropping Authorization properties when redirecting.")
      result = result.copy(authorization = null)
    }
    var headers = this.toLowercase(result.headers)
    if (headers.containsKey("authorization")) {
      this.logger.warn("Dropping Authorization header when redirecting.")
      headers = headers.minus("authorization")
    }
    return result.copy(headers = headers.toSortedMap())
  }

  private fun toLowercase(headers: SortedMap<String, String>): Map<String, String> {
    return headers.mapKeys { e -> e.key.lowercase() }
  }
}
