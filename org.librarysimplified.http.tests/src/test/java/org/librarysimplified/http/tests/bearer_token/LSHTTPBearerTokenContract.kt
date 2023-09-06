package org.librarysimplified.http.tests.bearer_token

import android.content.Context
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPAuthorizationBearerToken
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.AllowRedirects.ALLOW_REDIRECTS
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.bearer_token.LSHTTPBearerTokenInterceptors
import org.librarysimplified.http.tests.LSHTTPTestDirectories
import org.librarysimplified.http.vanilla.LSHTTPProblemReportParsers
import org.mockito.Mockito
import java.io.File
import java.util.concurrent.TimeUnit

abstract class LSHTTPBearerTokenContract {

  private lateinit var serverElsewhere: MockWebServer
  private lateinit var directory: File
  private lateinit var server: MockWebServer
  private lateinit var configuration: LSHTTPClientConfiguration
  private lateinit var context: Context

  abstract fun clients(
    parsers: LSHTTPProblemReportParserFactoryType = LSHTTPProblemReportParsers(),
  ): LSHTTPClientProviderType

  @BeforeEach
  fun testSetup() {
    this.context = Mockito.mock(Context::class.java)
    this.server = MockWebServer()
    this.serverElsewhere = MockWebServer()
    this.directory = LSHTTPTestDirectories.createTempDirectory()

    this.configuration =
      LSHTTPClientConfiguration(
        applicationName = "HttpTests",
        applicationVersion = "1.0.0",
      )
  }

  @AfterEach
  fun testTearDown() {
    this.server.shutdown()
    this.serverElsewhere.shutdown()
  }

  /**
   * A simple request to a real server.
   */

  @Test
  fun testClientRequestSimple() {
    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest("https://www.example.com")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals("text/html", status.properties.contentType.fullType)
      Assertions.assertEquals(null, status.properties.authorization)
    }
  }

  /**
   * A bearer token is parsed and used correctly.
   */

  @Test
  fun testClientRequestBearerTokenOK() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", LSHTTPBearerTokenInterceptors.bearerTokenContentType)
        .setBody(
          """
{
  "access_token": "abcd",
  "expires_in": 1000,
  "location": "${this.server.url("/abc")}"
}
          """.trimIndent(),
        ),
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("OK!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.properties.status)
      Assertions.assertEquals("Bearer abcd", status.properties.authorization?.toHeaderValue())
      Assertions.assertEquals("OK!", String(status.bodyStream!!.readBytes()))
    }

    val sent0 = this.server.takeRequest()
    Assertions.assertEquals(null, sent0.getHeader("Authorization"))
    val sent1 = this.server.takeRequest()
    Assertions.assertEquals("Bearer abcd", sent1.getHeader("Authorization"))
  }

  /**
   * When a bearer token is received, the request properties passed to a request modifier are
   * updated to reflect it.
   */

  @Test
  fun testPropertiesUpdated() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", LSHTTPBearerTokenInterceptors.bearerTokenContentType)
        .setBody(
          """
{
  "access_token": "abcd",
  "expires_in": 1000,
  "location": "${this.server.url("/abc")}"
}
          """.trimIndent(),
        ),
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("OK!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)

    val requestModifier = mock<(LSHTTPRequestProperties) -> LSHTTPRequestProperties>() {
      on { invoke(anyOrNull()) } doAnswer { it.getArgument(0) }
    }

    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setAuthorization(LSHTTPAuthorizationBearerToken.ofToken("original_token"))
        .setRequestModifier(requestModifier)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.properties.status)
      Assertions.assertEquals("Bearer abcd", status.properties.authorization?.toHeaderValue())
      Assertions.assertEquals("OK!", String(status.bodyStream!!.readBytes()))
    }

    val sent0 = this.server.takeRequest()
    Assertions.assertEquals("Bearer original_token", sent0.getHeader("Authorization"))
    val sent1 = this.server.takeRequest()
    Assertions.assertEquals("Bearer abcd", sent1.getHeader("Authorization"))

    inOrder(requestModifier) {
      verify(requestModifier).invoke(
        argThat {
          authorization!!.toHeaderValue() == "Bearer original_token"
        },
      )

      verify(requestModifier).invoke(
        argThat {
          authorization!!.toHeaderValue() == "Bearer abcd"
        },
      )
    }
  }

  /**
   * Unparseable bearer tokens result in 499 error codes.
   */

  @Test
  fun testUnparseable0() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", LSHTTPBearerTokenInterceptors.bearerTokenContentType)
        .setBody(
          """
{
  Two hundred years ago, during the Marathon's maiden voyage from Earth to Tau Ceti, Tycho accused 
  me of being too sarcastic. I didn't communicate with him for six years after that, which left him 
  with only Leela to talk to. I think he still holds the grudge. You don't think I'm sarcastic, 
  do you?
}
          """.trimIndent(),
        ),
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("OK!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.Error
      Assertions.assertEquals(499, status.properties.status)
      Assertions.assertEquals(null, status.properties.authorization)
      Assertions.assertTrue(status.properties.message.contains("Bearer token interceptor (LSHTTPBearerTokenInterceptor) parser failed"))
    }

    val sent0 = this.server.takeRequest()
    Assertions.assertEquals(null, sent0.getHeader("Authorization"))
    Assertions.assertEquals(null, this.server.takeRequest(1L, TimeUnit.SECONDS))
  }

  /**
   * A real bearer token request works.
   */

  @Test
  @Disabled("The real URI is no longer usable, and no replacement has been found yet.")
  fun testRealRequest() {
    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest("https://circulation.librarysimplified.org/CLASSICS/works/313533/fulfill/17")
        .allowRedirects(ALLOW_REDIRECTS)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.properties.status)
      Assertions.assertEquals("application/epub+zip", status.properties.contentType.fullType)
      Assertions.assertEquals(null, status.properties.authorization)
      Assertions.assertTrue(status.properties.contentLength ?: 0L >= 270000L)
    }
  }
}
