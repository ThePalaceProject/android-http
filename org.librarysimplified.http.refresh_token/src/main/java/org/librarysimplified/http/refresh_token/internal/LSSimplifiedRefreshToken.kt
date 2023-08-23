package org.librarysimplified.http.refresh_token.internal

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
class LSSimplifiedRefreshToken(
  @JvmField
  @JsonProperty("accessToken")
  val accessToken: String
)
