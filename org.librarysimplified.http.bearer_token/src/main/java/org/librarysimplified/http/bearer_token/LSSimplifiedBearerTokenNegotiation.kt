package org.librarysimplified.http.bearer_token

import org.librarysimplified.http.api.LSHTTPAuthorizationType
import org.librarysimplified.http.api.LSHTTPClientType
import org.librarysimplified.http.api.LSHTTPProblemReport
import org.librarysimplified.http.api.LSHTTPRequestBuilderType
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.refresh_token.LSHTTPRefreshAccessToken
import org.librarysimplified.http.refresh_token.LSHTTPRefreshTokenProperties
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI

/**
 * Functions to negotiate a Library Simplified Bearer token.
 */

object LSSimplifiedBearerTokenNegotiation {

  private val logger =
    LoggerFactory.getLogger(LSSimplifiedBearerTokenNegotiation::class.java)

  /**
   * The result of attempting to negotiate a token.
   */

  sealed interface NegotiationResultType

  /**
   * Negotiating a token succeeded.
   */

  data class NegotiationSucceeded(
    val token: LSSimplifiedBearerToken,
    val refreshToken: LSHTTPRefreshAccessToken?,
  ) : NegotiationResultType

  /**
   * Negotiating a token failed.
   */

  data class NegotiationFailed(
    val problemReport: LSHTTPProblemReport?,
    val exception: Exception?,
    val message: String,
    val response: LSHTTPResponseStatus.Responded.Error?,
  ) : NegotiationResultType

  /**
   * Negotiate a bearer token.
   */

  fun negotiate(
    client: LSHTTPClientType,
    target: URI,
    refreshTokenProperties: LSHTTPRefreshTokenProperties?,
    authorization: LSHTTPAuthorizationType?,
  ): NegotiationResultType {
    this.logger.debug("Negotiating a bearer token at {}", target)

    val requestBuilder =
      client.newRequest(target)
        .setMethod(LSHTTPRequestBuilderType.Method.Get)
        .setAuthorization(authorization)

    if (refreshTokenProperties != null) {
      LSHTTPRefreshTokenProperties.addToRequestBuilder(requestBuilder, refreshTokenProperties)
    }

    val request =
      requestBuilder.build()

    return request.execute().use { response ->
      when (val status = response.status) {
        is LSHTTPResponseStatus.Responded.OK -> {
          this.handleOKRequest(status)
        }

        is LSHTTPResponseStatus.Responded.Error -> {
          this.handleHTTPError(status)
        }

        is LSHTTPResponseStatus.Failed -> {
          this.handleHTTPFailure(status)
        }
      }
    }
  }

  private fun handleHTTPFailure(
    status: LSHTTPResponseStatus.Failed,
  ): NegotiationResultType {
    this.logger.debug("Failed to negotiate a bearer token.")
    return NegotiationFailed(
      problemReport = null,
      exception = status.exception,
      message = status.exception.message ?: "Exception raised during connection attempt.",
      response = null,
    )
  }

  private fun handleHTTPError(
    status: LSHTTPResponseStatus.Responded.Error,
  ): NegotiationResultType {
    this.logger.debug("Failed to negotiate a bearer token.")
    return NegotiationFailed(
      problemReport = status.properties.problemReport,
      exception = null,
      message = "Server returned an error status code.",
      response = status,
    )
  }

  private fun handleOKRequest(
    status: LSHTTPResponseStatus.Responded.OK,
  ): NegotiationResultType {
    return try {
      val token =
        LSSimplifiedBearerTokenJSON.deserializeFromStream(
          status.bodyStream ?: ByteArrayInputStream(ByteArray(0)),
        )
      this.logger.debug("Successfully negotiated a bearer token.")
      NegotiationSucceeded(
        token = token,
        refreshToken = LSHTTPRefreshTokenProperties.accessTokenOf(status),
      )
    } catch (e: Exception) {
      NegotiationFailed(
        problemReport = status.properties.problemReport,
        exception = e,
        message = "Received an exception whilst trying to parse a bearer token.",
        response = null,
      )
    }
  }
}
