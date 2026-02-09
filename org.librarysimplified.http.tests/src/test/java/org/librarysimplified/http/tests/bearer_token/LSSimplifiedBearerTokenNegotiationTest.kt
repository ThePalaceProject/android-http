package org.librarysimplified.http.tests.bearer_token

import android.content.Context
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.bearer_token.LSSimplifiedBearerTokenNegotiation
import org.librarysimplified.http.refresh_token.LSHTTPRefreshTokenProperties
import org.librarysimplified.http.vanilla.LSHTTPClients
import org.librarysimplified.http.vanilla.LSHTTPProblemReportParsers
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

class LSSimplifiedBearerTokenNegotiationTest {

  private val logger =
    LoggerFactory.getLogger(LSSimplifiedBearerTokenNegotiationTest::class.java)

  private lateinit var server: MockWebServer
  private lateinit var clients: LSHTTPClients

  @BeforeEach
  fun setup() {
    this.server = MockWebServer()
    this.server.start(30000)
    this.clients = LSHTTPClients(
      LSHTTPProblemReportParsers(),
      listOf(),
    )
  }

  @AfterEach
  fun tearDown() {
    try {
      this.server.shutdown()
    } catch (e: Exception) {
      this.logger.debug("shutting down server: ", e)
    }
  }

  @Test
  fun testNegotiateConnectFailed() {
    this.server.shutdown()

    val configuration =
      LSHTTPClientConfiguration(
        applicationName = "HttpTests",
        applicationVersion = "1.0.0",
      )

    val client =
      this.clients.create(
        context = Mockito.mock(Context::class.java),
        configuration = configuration
      )

    val r =
      LSSimplifiedBearerTokenNegotiation.negotiate(
        client = client,
        target = URI.create("http://localhost:30000"),
        refreshTokenProperties = LSHTTPRefreshTokenProperties(
          userName = "example",
          password = "password",
          refreshURI = URI.create("http://localhost:30000/refresh")
        ),
        authorization = null
      )

    val e = assertInstanceOf(
      LSSimplifiedBearerTokenNegotiation.NegotiationFailed::class.java,
      r
    )
    assertNull(e.response)
    assertNull(e.problemReport)
    assertInstanceOf(IOException::class.java, e.exception)
    assertTrue(e.message.startsWith("Failed to connect"))
  }

  @Test
  fun testNegotiateError() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(500)
    )

    val configuration =
      LSHTTPClientConfiguration(
        applicationName = "HttpTests",
        applicationVersion = "1.0.0",
      )

    val client =
      this.clients.create(
        context = Mockito.mock(Context::class.java),
        configuration = configuration
      )

    val r =
      LSSimplifiedBearerTokenNegotiation.negotiate(
        client = client,
        target = URI.create("http://localhost:30000"),
        refreshTokenProperties = LSHTTPRefreshTokenProperties(
          userName = "example",
          password = "password",
          refreshURI = URI.create("http://localhost:30000/refresh")
        ),
        authorization = null
      )

    val e = assertInstanceOf(
      LSSimplifiedBearerTokenNegotiation.NegotiationFailed::class.java,
      r
    )
    val response = e.response!!
    assertEquals(500, response.properties.status)
    assertNull(e.problemReport)
    assertNull(e.exception)
    assertEquals(e.message, "Server returned an error status code.")
  }

  @Test
  fun testNegotiateOK() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("""
{
  "access_token": "abcd",
  "expires_in": 1000,
  "location": "https://www.example.com"
}
        """.trimIndent())
    )

    val configuration =
      LSHTTPClientConfiguration(
        applicationName = "HttpTests",
        applicationVersion = "1.0.0",
      )

    val client =
      this.clients.create(
        context = Mockito.mock(Context::class.java),
        configuration = configuration
      )

    val r =
      LSSimplifiedBearerTokenNegotiation.negotiate(
        client = client,
        target = URI.create("http://localhost:30000"),
        refreshTokenProperties = LSHTTPRefreshTokenProperties(
          userName = "example",
          password = "password",
          refreshURI = URI.create("http://localhost:30000/refresh")
        ),
        authorization = null
      )

    val e = assertInstanceOf(
      LSSimplifiedBearerTokenNegotiation.NegotiationSucceeded::class.java,
      r
    )
    assertEquals("abcd", e.token.accessToken)
    assertEquals(1000, e.token.expiresIn.toInt())
    assertEquals("https://www.example.com", e.token.location.toString())
  }

  @Test
  fun testNegotiateUnparseableToken() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("""
{

}
        """.trimIndent())
    )

    val configuration =
      LSHTTPClientConfiguration(
        applicationName = "HttpTests",
        applicationVersion = "1.0.0",
      )

    val client =
      this.clients.create(
        context = Mockito.mock(Context::class.java),
        configuration = configuration
      )

    val r =
      LSSimplifiedBearerTokenNegotiation.negotiate(
        client = client,
        target = URI.create("http://localhost:30000"),
        refreshTokenProperties = LSHTTPRefreshTokenProperties(
          userName = "example",
          password = "password",
          refreshURI = URI.create("http://localhost:30000/refresh")
        ),
        authorization = null
      )

    val e = assertInstanceOf(
      LSSimplifiedBearerTokenNegotiation.NegotiationFailed::class.java,
      r
    )
    assertNull(e.response)
    assertNull(e.problemReport)
    assertEquals("Received an exception whilst trying to parse a bearer token.", e.message)
    assertInstanceOf(MismatchedInputException::class.java, e.exception)
  }
}
