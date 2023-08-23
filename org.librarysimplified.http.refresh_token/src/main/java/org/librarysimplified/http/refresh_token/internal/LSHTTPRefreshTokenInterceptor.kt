package org.librarysimplified.http.refresh_token.internal

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.librarysimplified.http.api.LSHTTPAuthorizationBasic
import org.librarysimplified.http.api.LSHTTPAuthorizationBearerToken
import org.librarysimplified.http.api.LSHTTPRequestConstants
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection

class LSHTTPRefreshTokenInterceptor : Interceptor {

  private val logger =
    LoggerFactory.getLogger(LSHTTPRefreshTokenInterceptor::class.java)

  private fun handleRefreshTokenProperties(
    chain: Interceptor.Chain,
    originalResponse: Response,
    originalRequest: Request,
    properties: LSHTTPRequestProperties?
  ): Response {
    properties ?: return originalResponse

    val userName =
      properties.otherProperties[LSHTTPRequestConstants.PROPERTY_KEY_USERNAME] as? String
    val password =
      properties.otherProperties[LSHTTPRequestConstants.PROPERTY_KEY_PASSWORD] as? String
    val authenticationUrl =
      properties.otherProperties[LSHTTPRequestConstants.PROPERTY_KEY_AUTHENTICATION_URL] as? String

    return if (
      !userName.isNullOrBlank() && !password.isNullOrBlank() && !authenticationUrl.isNullOrBlank()
    ) {

      this.logger.debug("We have a username, a password and an auth url, so let's refresh the token")

      val newRequest = originalRequest
        .newBuilder()
        .header(  
          "Authorization",
          LSHTTPAuthorizationBasic.ofUsernamePassword(userName, password).toHeaderValue()
        )
        .url(authenticationUrl)
        .build()

      originalResponse.close()
      val response = chain.proceed(newRequest)
      if (response.isSuccessful) {
        try {
          val accessToken =
            response.body?.byteStream()
              .use(LSSimplifiedRefreshTokenJSON::deserializeFromStream)
              .accessToken

          response.close()

          val newResponse = chain.proceed(
            originalRequest
              .newBuilder()
              .header(
                "Authorization",
                LSHTTPAuthorizationBearerToken.ofToken(accessToken).toHeaderValue()
              )
              .build()
          )

          newResponse.newBuilder()
            .addHeader(LSHTTPRequestConstants.PROPERTY_KEY_ACCESS_TOKEN, accessToken)
            .build()
        } catch (e: Exception) {
          chain.proceed(originalRequest)
        }
      } else {
        response
      }
    } else {
      originalResponse
    }
  }

  override fun intercept(
    chain: Interceptor.Chain
  ): Response {
    val originalRequest = chain.request()
    val response = chain.proceed(originalRequest)

    return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
      val properties = originalRequest.tag(LSHTTPRequestProperties::class.java)
      handleRefreshTokenProperties(
        chain = chain,
        originalRequest = originalRequest,
        originalResponse = response,
        properties = properties
      )
    } else {
      response
    }
  }
}
