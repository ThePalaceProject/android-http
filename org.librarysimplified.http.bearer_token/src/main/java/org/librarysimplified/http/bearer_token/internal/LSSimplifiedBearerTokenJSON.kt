package org.librarysimplified.http.bearer_token.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.librarysimplified.http.bearer_token.LSSimplifiedBearerToken
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Functions to serialize/deserialize bearer tokens.
 */

object LSSimplifiedBearerTokenJSON {

  fun serializeToJSON(
    objectMapper: ObjectMapper,
    token: LSSimplifiedBearerToken,
  ): ObjectNode {
    val node = objectMapper.createObjectNode()
    node.put("access_token", token.accessToken)
    node.put("expires_in", token.expiresIn)
    node.put("location", token.location.toString())
    return node
  }

  fun serializeToText(
    objectMapper: ObjectMapper,
    token: LSSimplifiedBearerToken,
  ): String {
    return ByteArrayOutputStream().use { stream ->
      val writer = objectMapper.writerWithDefaultPrettyPrinter()
      writer.writeValue(stream, serializeToJSON(objectMapper, token))
      stream.toString("UTF-8")
    }
  }

  fun serializeToText(
    token: LSSimplifiedBearerToken,
  ): String {
    return serializeToText(
      objectMapper = LSSimplifiedBearerTokenObjectMappers.createObjectMapper(),
      token = token,
    )
  }

  fun deserializeFromStream(
    objectMapper: ObjectMapper,
    stream: InputStream,
  ): LSSimplifiedBearerToken {
    val factory = objectMapper.factory
    return factory.createParser(stream).use {
      objectMapper.readValue(it, LSSimplifiedBearerToken::class.java)
    }
  }

  fun deserializeFromStream(
    stream: InputStream,
  ): LSSimplifiedBearerToken {
    return deserializeFromStream(
      objectMapper = LSSimplifiedBearerTokenObjectMappers.createObjectMapper(),
      stream = stream,
    )
  }

  fun deserializeFromText(
    objectMapper: ObjectMapper,
    text: String,
  ): LSSimplifiedBearerToken {
    return deserializeFromStream(
      objectMapper = objectMapper,
      stream = text.byteInputStream(),
    )
  }

  fun deserializeFromText(
    text: String,
  ): LSSimplifiedBearerToken {
    return deserializeFromText(
      objectMapper = LSSimplifiedBearerTokenObjectMappers.createObjectMapper(),
      text = text,
    )
  }
}
