package org.librarysimplified.http.oauth_client_credentials

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.joda.time.LocalDateTime
import java.math.BigInteger

/**
 * The type of bearer tokens.
 */

@JsonDeserialize
data class LSHTTPOAuthClientCredentialsToken(
  @JsonProperty(value = "access_token", required = true)
  val accessToken: String,

  @JsonProperty(value = "expires_in", required = true)
  val expiresIn: BigInteger,
) {

  /**
   * @return The time at which this bearer token expires
   */

  fun expiresAt(now: LocalDateTime): LocalDateTime {
    return now.plusSeconds(this.expiresIn.toInt())
  }
}
