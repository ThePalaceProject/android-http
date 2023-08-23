package org.librarysimplified.http.vanilla.internal

import okhttp3.Interceptor
import okhttp3.Response
import org.librarysimplified.http.api.LSHTTPResponseType

class LSHTTPRedirectResponseInterceptor(
  private val client: LSHTTPClient,
  private val observer: (LSHTTPResponseType) -> Unit,
) : Interceptor {

  override fun intercept(
    chain: Interceptor.Chain,
  ): Response {
    val response = chain.proceed(chain.request())
    this.observer.invoke(LSHTTPResponse.ofOkResponse(this.client, response))
    return response
  }
}
