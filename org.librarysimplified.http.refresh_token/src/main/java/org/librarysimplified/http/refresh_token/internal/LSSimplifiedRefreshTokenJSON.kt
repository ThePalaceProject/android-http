package org.librarysimplified.http.refresh_token.internal

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream

/**
 * Functions to deserialize an access token.
 */

object LSSimplifiedRefreshTokenJSON {
  fun deserializeFromStream(
    stream: InputStream,
  ): LSSimplifiedRefreshToken {
    val objectMapper = ObjectMapper()
    return try {
      objectMapper.readValue(stream, LSSimplifiedRefreshToken::class.java)
    } catch (e: Exception) {
      throw e
    }
  }
}
