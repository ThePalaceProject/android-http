package org.librarysimplified.http.bearer_token.internal

import okhttp3.Interceptor
import okhttp3.Request
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
    chain: Interceptor.Chain,
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
        val wasRedirected = !response.request.url.canReuseConnectionFor(newRequest0.url)

        /*
         * Some servers may require a downgrade from HTTPS to HTTP. If this happens, `okhttp`
         * will refuse to do it and will return a redirect response here. We handle this
         * explicitly by making a new request without authorization information.
         */
        if (innerResponse.isRedirect) {
          val target =
            innerResponse.header("Location") ?: innerResponse.request.url.toString()
          this.logger.warn("handling HTTP downgrade redirect explicitly {}", target)

          // close the inner response so we can avoid having more than one open
          innerResponse.close()

          proceedWithNewRequest(chain, newRequest0, target)
        } else if (!innerResponse.isSuccessful && wasRedirected) {
          /*
           * Some books may be hosted on a server (e.g. AWS) that errors when an authorization header
           * is sent. In this case, okhttp may return a bad request response (code 400) because it
           * internally handled a redirection to that server, and attached the authorization intended
           * for a host earlier in the redirect chain. When this happens we retry a request but without
           * the authorization header if two conditions are met: the request was unsuccessful and
           * there's been a redirection.
           *
           * TODO: This can be avoided if we prevent authorization headers from being sent through
           * redirects where the host changes. This currently happens in LSHTTPRedirectRequestInterceptor,
           * which copies the request properties (including authorization) to the next request. We would
           * need to associate the authorization in the properties with an intended host, and only send
           * an authorization header when the authorization's intended host matches the target host of
           * the request.
           */

          val target =
            innerResponse.header("Location") ?: innerResponse.request.url.toString()
          this.logger.warn("handling an unsuccessful redirection {}", target)

          // close the inner response so we can avoid having more than one open
          innerResponse.close()

          proceedWithNewRequest(chain, newRequest0, target)
        } else {
          innerResponse
        }
      } catch (e: Exception) {
        this.errorBadBearerToken(response, e)
      }
    }

    return response
  }

  private fun proceedWithNewRequest(
    chain: Interceptor.Chain,
    oldRequest: Request,
    target: String,
  ): Response {
    val request0Properties = oldRequest.tag(LSHTTPRequestProperties::class.java)!!
    val request1Properties = request0Properties.copy(authorization = null)

    val newRequest1 =
      oldRequest.newBuilder()
        .tag(LSHTTPRequestProperties::class.java, request1Properties)
        .url(target)
        .build()

    return chain.proceed(newRequest1)
  }

  private fun errorBadBearerToken(
    response: Response,
    exception: Exception,
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
