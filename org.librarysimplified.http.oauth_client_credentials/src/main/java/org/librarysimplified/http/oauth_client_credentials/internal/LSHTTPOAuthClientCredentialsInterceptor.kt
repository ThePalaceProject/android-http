package org.librarysimplified.http.oauth_client_credentials.internal

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.joda.time.LocalDateTime
import org.librarysimplified.http.api.LSHTTPAuthorizationBearerToken
import org.librarysimplified.http.api.LSHTTPAuthorizationType
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.oauth_client_credentials.LSHTTPOAuthClientCredentialsToken
import org.librarysimplified.http.oauth_client_credentials.oauthAuthenticateURI
import org.slf4j.LoggerFactory

/**
 * An interceptor to use OAuth Client Credentials authentication when possible.
 *
 * If an authentication URI has been set on LSHTTPRequestBuilderType
 * with the setOAuthAuthenticateURI extension, the original request authorization will be used
 * to get a bearer token from that URI and the original request will be proceed with that token.
 * The token will be cached for reuse by subsequent requests until its expiration time.
 *
 * This is originally implemented for FirstBook authentication in OpenEbooks.
 */

internal class LSHTTPOAuthClientCredentialsInterceptor(
  private val tokenRepository: LSHTTPOAuthTokenRepository<RepositoryKey, String>,
) : Interceptor {

  internal data class RepositoryKey(
    val authenticateURI: String,
    val originalAuthorization: String,
  )

  private val logger =
    LoggerFactory.getLogger(LSHTTPOAuthClientCredentialsInterceptor::class.java)

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val properties = originalRequest.tag(LSHTTPRequestProperties::class.java)!!
    val authenticateURI = properties.oauthAuthenticateURI?.toString()
    val originalAuthorization = properties.authorization

    // Check if we should use OAuth Client Credentials
    return if (authenticateURI == null || originalAuthorization == null) {
      chain.proceed(originalRequest)
    } else {
      proceedWithOAuthClientCredentials(
        chain,
        originalRequest,
        authenticateURI,
        originalAuthorization,
      )
    }
  }

  private fun proceedWithOAuthClientCredentials(
    chain: Interceptor.Chain,
    originalRequest: Request,
    authenticateURI: String,
    originalAuthorization: LSHTTPAuthorizationType,
  ): Response {
    logger.debug("Intercepting request to ${originalRequest.url}.")

    // Get the access last token, proceed the original request if we can't get one.
    return try {
      val token = getToken(chain, authenticateURI, originalAuthorization)
      proceedWithOAuthToken(chain, originalRequest, token)
    } catch (e: Exception) {
      logger.error("No valid OAuth Client Credentials token. Proceeding original request.")
      chain.proceed(originalRequest)
    }
  }

  private fun getToken(
    chain: Interceptor.Chain,
    authenticateURI: String,
    originalAuthorization: LSHTTPAuthorizationType,
  ): String {
    val oneMinuteAgo = LocalDateTime.now().minusMinutes(1)
    val key = RepositoryKey(authenticateURI, originalAuthorization.toHeaderValue())
    return tokenRepository.getOrRefresh(key) {
      val token = fetchToken(chain, authenticateURI, originalAuthorization)
      val expiration = token.expiresAt(oneMinuteAgo)
      logger.debug("New token will expire at $expiration.")
      LSHTTPOAuthTokenRepository.Expirable(token.accessToken, expiration)
    }
  }

  private fun proceedWithOAuthToken(
    chain: Interceptor.Chain,
    originalRequest: Request,
    token: String,
  ): Response {
    // Proceed the request with the access token.
    val newAuthorization =
      LSHTTPAuthorizationBearerToken.ofToken(token)

    val newRequest =
      originalRequest
        .newBuilder()
        .header("Authorization", newAuthorization.toHeaderValue())
        .build()

    this.logger.debug("Sending a new request to {}", newRequest.url)

    return chain.proceed(newRequest)
  }

  private fun fetchToken(
    chain: Interceptor.Chain,
    authenticateURI: String,
    authorization: LSHTTPAuthorizationType?,
  ): LSHTTPOAuthClientCredentialsToken {
    logger.debug("Trying to fetch a new token for $authenticateURI")

    val authRequest = Request.Builder()
      .url(authenticateURI)
      .get()
      .apply { authorization?.let { header("Authorization", it.toHeaderValue()) } }
      .build()

    val response =
      chain.proceed(authRequest)

    val body =
      response.body
        ?: throw Exception("A token was expected but an empty body was received from the server.")

    return body.byteStream()
      .use(LSHTTPOAuthClientCredentialsTokenJSON::deserializeFromStream)
  }
}
