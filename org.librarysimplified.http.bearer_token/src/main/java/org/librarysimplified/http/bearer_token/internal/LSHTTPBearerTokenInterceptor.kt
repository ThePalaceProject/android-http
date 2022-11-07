package org.librarysimplified.http.bearer_token.internal

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.canReuseConnectionFor
import org.librarysimplified.http.api.LSHTTPAuthorizationBearerToken
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.bearer_token.LSHTTPBearerTokenInterceptors.Companion.bearerTokenContentType
import org.slf4j.LoggerFactory

class LSHTTPBearerTokenInterceptor : Interceptor {

  private val logger =
    LoggerFactory.getLogger(LSHTTPBearerTokenInterceptor::class.java)

  override fun intercept(
    chain: Interceptor.Chain
  ): Response {
    val originalRequest = chain.request()
    val response = chain.proceed(originalRequest)
    if (response.header("content-type") == bearerTokenContentType) {
      this.logger.debug("encountered a bearer token for {}", originalRequest.url)
      val body = response.body ?: return this.errorEmptyBody(response)
      return try {
        val token =
          body.byteStream()
            .use(LSSimplifiedBearerTokenJSON::deserializeFromStream)

        val properties = originalRequest.tag(LSHTTPRequestProperties::class.java)!!
        val newAuthorization = LSHTTPAuthorizationBearerToken.ofToken(token.accessToken)
        val newProperties = properties.copy(authorization = newAuthorization)

        val newRequest0 =
          originalRequest
            .newBuilder()
            .header("Authorization", newAuthorization.toHeaderValue())
            .tag(LSHTTPRequestProperties::class.java, newProperties)
            .url(token.location.toString())
            .build()

        this.logger.debug("sending a new request to {}", newRequest0.url)
        val innerResponse = chain.proceed(newRequest0)

        /*
         * Some servers may require a downgrade from HTTPS to HTTP. If this happens, `okhttp`
         * will refuse to do it and will return a redirect response here. We handle this
         * explicitly by making a new request without authorization information.
         */

        val wasRedirected = !response.request.url.canReuseConnectionFor(newRequest0.url)

        if (innerResponse.isRedirect || (!innerResponse.isSuccessful && wasRedirected)) {
          val target =
            innerResponse.header("Location") ?: innerResponse.request.url.toString()
          this.logger.warn("handling HTTP downgrade redirect explicitly {}", target)

          // close the inner response so we can avoid having more than one open
          innerResponse.close()

          val request0Properties = newRequest0.tag(LSHTTPRequestProperties::class.java)!!
          val request1Properties = request0Properties.copy(authorization = null)

          val newRequest1 =
            newRequest0.newBuilder()
              .removeHeader("Authorization")
              .tag(LSHTTPRequestProperties::class.java, request1Properties)
              .url(target)
              .build()

          chain.proceed(newRequest1)
        } else {
          innerResponse
        }
      } catch (e: Exception) {
        this.errorBadBearerToken(response, e)
      }
    }

    return response
  }

  private fun errorBadBearerToken(
    response: Response,
    exception: Exception
  ): Response {
    this.logger.error("could not parse bearer token: ", exception)
    return response.newBuilder()
      .code(499)
      .message("Bearer token interceptor (LSHTTPBearerTokenInterceptor) parser failed: ${exception.message}")
      .build()
  }

  private fun errorEmptyBody(response: Response): Response {
    this.logger.warn("received empty body from server")
    return response.newBuilder()
      .code(499)
      .message("Bearer token interceptor (LSHTTPBearerTokenInterceptor) received an empty body from the server!")
      .build()
  }
}
