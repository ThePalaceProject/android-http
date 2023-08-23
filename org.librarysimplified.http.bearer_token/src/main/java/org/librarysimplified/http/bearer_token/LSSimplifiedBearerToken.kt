package org.librarysimplified.http.bearer_token

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.joda.time.LocalDateTime
import java.math.BigInteger
import java.net.URI

/**
 * The type of bearer tokens.
 */

@JsonDeserialize
data class LSSimplifiedBearerToken(
  @JsonProperty(value = "access_token", required = true)
  val accessToken: String,

  @JsonProperty(value = "expires_in", required = true)
  val expiresIn: BigInteger,

  @JsonProperty(value = "location", required = true)
  val location: URI,
) {

  /**
   * @return The time at which this bearer token expires
   */

  fun expiresAt(now: LocalDateTime): LocalDateTime {
    return now.plusSeconds(this.expiresIn.toInt())
  }
}
