package org.librarysimplified.http.vanilla.internal

import okhttp3.Interceptor
import okhttp3.Response
import org.librarysimplified.http.api.LSHTTPRequestProperties

class LSHTTPRedirectRequestInterceptor(
  private val modifier: ((LSHTTPRequestProperties) -> LSHTTPRequestProperties)?,
) : Interceptor {

  override fun intercept(
    chain: Interceptor.Chain,
  ): Response {
    val request =
      chain.request()

    val properties =
      request.tag(LSHTTPRequestProperties::class.java)!!
    val adjProperties =
      properties.copy(target = request.url.toUri())
    val newProperties = if (this.modifier != null) {
      this.modifier.invoke(adjProperties)
    } else {
      adjProperties
    }

    val requestBuilder =
      request.newBuilder()
    val newRequest =
      LSOKHTTPRequests.createRequestForBuilder(newProperties, requestBuilder)

    return chain.proceed(newRequest)
  }
}
