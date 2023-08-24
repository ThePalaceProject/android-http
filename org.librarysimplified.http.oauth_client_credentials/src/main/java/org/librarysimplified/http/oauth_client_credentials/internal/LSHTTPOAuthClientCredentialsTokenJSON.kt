package org.librarysimplified.http.oauth_client_credentials.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.librarysimplified.http.oauth_client_credentials.LSHTTPOAuthClientCredentialsToken
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Functions to serialize/deserialize bearer tokens.
 */

object LSHTTPOAuthClientCredentialsTokenJSON {

  fun serializeToJSON(
    objectMapper: ObjectMapper,
    token: LSHTTPOAuthClientCredentialsToken,
  ): ObjectNode {
    val node = objectMapper.createObjectNode()
    node.put("access_token", token.accessToken)
    node.put("expires_in", token.expiresIn)
    return node
  }

  fun serializeToText(
    objectMapper: ObjectMapper,
    token: LSHTTPOAuthClientCredentialsToken,
  ): String {
    return ByteArrayOutputStream().use { stream ->
      val writer = objectMapper.writerWithDefaultPrettyPrinter()
      writer.writeValue(stream, serializeToJSON(objectMapper, token))
      stream.toString("UTF-8")
    }
  }

  fun serializeToText(
    token: LSHTTPOAuthClientCredentialsToken,
  ): String {
    return serializeToText(
      objectMapper = LSHTTPOAuthClientCredentialsObjectMappers.createObjectMapper(),
      token = token,
    )
  }

  fun deserializeFromStream(
    objectMapper: ObjectMapper,
    stream: InputStream,
  ): LSHTTPOAuthClientCredentialsToken {
    val factory = objectMapper.factory
    return factory.createParser(stream).use {
      objectMapper.readValue(it, LSHTTPOAuthClientCredentialsToken::class.java)
    }
  }

  fun deserializeFromStream(
    stream: InputStream,
  ): LSHTTPOAuthClientCredentialsToken {
    return deserializeFromStream(
      objectMapper = LSHTTPOAuthClientCredentialsObjectMappers.createObjectMapper(),
      stream = stream,
    )
  }

  fun deserializeFromText(
    objectMapper: ObjectMapper,
    text: String,
  ): LSHTTPOAuthClientCredentialsToken {
    return deserializeFromStream(
      objectMapper = objectMapper,
      stream = text.byteInputStream(),
    )
  }

  fun deserializeFromText(
    text: String,
  ): LSHTTPOAuthClientCredentialsToken {
    return deserializeFromText(
      objectMapper = LSHTTPOAuthClientCredentialsObjectMappers.createObjectMapper(),
      text = text,
    )
  }
}
