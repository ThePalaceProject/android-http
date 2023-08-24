package org.librarysimplified.http.tests.refresh_token

import android.content.Context
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPAuthorizationBasic
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPRequestConstants
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.tests.LSHTTPTestDirectories
import org.librarysimplified.http.vanilla.LSHTTPProblemReportParsers
import org.mockito.Mockito
import java.io.File

abstract class LSHTTPRefreshTokenContract {

  private lateinit var serverElsewhere: MockWebServer
  private lateinit var directory: File
  private lateinit var server: MockWebServer
  private lateinit var configuration: LSHTTPClientConfiguration
  private lateinit var context: Context

  abstract fun clients(
    parsers: LSHTTPProblemReportParserFactoryType = LSHTTPProblemReportParsers()
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
        applicationVersion = "1.0.0"
      )
  }

  @AfterEach
  fun testTearDown() {
    this.server.shutdown()
    this.serverElsewhere.shutdown()
  }

  @Test
  fun testClientRefreshTokenOK() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(401)
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("{ \"accessToken\": \"abcd\"}")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_USERNAME, "username")
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_PASSWORD, "password")
        .setExtensionProperty(
          LSHTTPRequestConstants.PROPERTY_KEY_AUTHENTICATION_URL,
          this.server.url("/auth").toString()
        )
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.properties.status)
    }

    val firstRequest = this.server.takeRequest()
    Assertions.assertEquals(null, firstRequest.getHeader("Authorization"))

    val secondRequest = this.server.takeRequest()
    val basic = LSHTTPAuthorizationBasic.ofUsernamePassword("username", "password")
    Assertions.assertEquals(this.server.url("/auth"), secondRequest.requestUrl)
    Assertions.assertEquals(basic.toHeaderValue(), secondRequest.getHeader("Authorization"))

    val thirdRequest = this.server.takeRequest()
    Assertions.assertEquals("Bearer abcd", thirdRequest.getHeader("Authorization"))
  }

  @Test
  fun testClientRefreshTokenMissingAuthUrl() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(401)
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_USERNAME, "username")
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_PASSWORD, "password")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.Error
      Assertions.assertEquals(401, status.properties.status)
    }

    // no further requests were made
    Assertions.assertEquals(this.server.requestCount, 1)

    val firstRequest = this.server.takeRequest()
    Assertions.assertEquals(null, firstRequest.getHeader("Authorization"))
  }

  @Test
  fun testClientRefreshTokenWrongToken() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(401)
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("{}")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(401)
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_USERNAME, "username")
        .setExtensionProperty(LSHTTPRequestConstants.PROPERTY_KEY_PASSWORD, "password")
        .setExtensionProperty(
          LSHTTPRequestConstants.PROPERTY_KEY_AUTHENTICATION_URL,
          this.server.url("/auth").toString()
        )
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.Error
      Assertions.assertEquals(401, status.properties.status)
    }

    val firstRequest = this.server.takeRequest()
    Assertions.assertEquals(null, firstRequest.getHeader("Authorization"))

    val secondRequest = this.server.takeRequest()
    val basic = LSHTTPAuthorizationBasic.ofUsernamePassword("username", "password")
    Assertions.assertEquals(basic.toHeaderValue(), secondRequest.getHeader("Authorization"))

    val thirdRequest = this.server.takeRequest()
    Assertions.assertEquals(null, thirdRequest.getHeader("Authorization"))
  }
}
