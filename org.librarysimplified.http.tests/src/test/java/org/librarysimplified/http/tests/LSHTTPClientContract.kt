package org.librarysimplified.http.tests

import android.content.Context
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPAuthorizationBasic
import org.librarysimplified.http.api.LSHTTPAuthorizationBearerToken
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.AllowRedirects.ALLOW_UNSAFE_REDIRECTS
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.AllowRedirects.DISALLOW_REDIRECTS
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.Method.Delete
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.Method.Head
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.Method.Post
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.Method.Put
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.api.LSHTTPTLSOverrides
import org.librarysimplified.http.vanilla.LSHTTPProblemReportParsers
import org.librarysimplified.http.vanilla.internal.LSHTTPMimeTypes
import org.librarysimplified.http.vanilla.internal.LSHTTPMimeTypes.octetStream
import org.mockito.Mockito
import java.io.File


abstract class LSHTTPClientContract {

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

    this.server.start(30000)
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
      Assertions.assertEquals("text/html", status.contentType.fullType)
    }
  }

  /**
   * A HEAD request works.
   */

  @Test
  fun testClientRequestHEAD() {
    this.server.enqueue(MockResponse().setResponseCode(200))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setMethod(Head)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val received = this.server.takeRequest()
    Assertions.assertEquals("HEAD", received.method)
    Assertions.assertEquals(0, received.bodySize)
  }

  /**
   * A HEAD request works.
   */

  @Test
  fun testClientRequestDELETE() {
    this.server.enqueue(MockResponse().setResponseCode(200))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setMethod(Delete)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val received = this.server.takeRequest()
    Assertions.assertEquals("DELETE", received.method)
    Assertions.assertEquals(0, received.bodySize)
  }

  /**
   * A POST request sends the body.
   */

  @Test
  fun testClientRequestPOST() {
    this.server.enqueue(MockResponse().setResponseCode(200))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setMethod(Post("Hello.".toByteArray(), LSHTTPMimeTypes.textPlain))
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val received = this.server.takeRequest()
    Assertions.assertEquals("POST", received.method)
    Assertions.assertEquals(6, received.bodySize)
    Assertions.assertEquals(LSHTTPMimeTypes.textPlain.fullType, received.getHeader("content-type"))
  }

  /**
   * A PUT request sends the body.
   */

  @Test
  fun testClientRequestPUT() {
    this.server.enqueue(MockResponse().setResponseCode(200))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setMethod(Put("Hello.".toByteArray(), LSHTTPMimeTypes.textPlain))
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val received = this.server.takeRequest()
    Assertions.assertEquals("PUT", received.method)
    Assertions.assertEquals(6, received.bodySize)
    Assertions.assertEquals(LSHTTPMimeTypes.textPlain.fullType, received.getHeader("content-type"))
  }

  /**
   * A 404 error code results in failure.
   */

  @Test
  fun testClientRequest404() {
    this.server.enqueue(MockResponse().setResponseCode(404))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.Error
      Assertions.assertEquals(404, status.status)
    }
  }

  /**
   * An unparseable content type yields application/octet-stream.
   */

  @Test
  fun testClientRequestContentTypeUnparseable() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "&gibberish ne cede malis")
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(octetStream.fullType, status.contentType.fullType)
      Assertions.assertArrayEquals("Hello.".toByteArray(), status.bodyStream!!.readBytes())
    }
  }

  /**
   * A problem report can turn success into failure.
   */

  @Test
  fun testClientRequestProblemReport() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", LSHTTPMimeTypes.problemReport.fullType)
        .setBody(
          LSHTTPTestDirectories.stringOf(
            LSHTTPTestDirectories::class.java,
            this.directory,
            "error.json"
          )
        )
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.Error
      val problemReport = status.problemReport!!
      Assertions.assertEquals(500, status.status)
      Assertions.assertEquals("https://example.com/probs/out-of-credit", problemReport.type)
      Assertions.assertEquals("You do not have enough credit.", problemReport.title)
      Assertions.assertEquals(
        "Your current balance is 30, but that costs 50.",
        problemReport.detail
      )
    }
  }

  /**
   * A problem report that cannot be parsed is ignored.
   */

  @Test
  fun testClientRequestProblemReportUnparseable() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", LSHTTPMimeTypes.problemReport.fullType)
        .setBody(
          LSHTTPTestDirectories.stringOf(
            LSHTTPTestDirectories::class.java,
            this.directory,
            "invalid0.json"
          )
        )
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }
  }

  /**
   * A request to an unresolvable address fails.
   */

  @Test
  fun testClientRequestUnresolvable() {
    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest("https://invalid")
        .build()

    request.execute().use { response ->
      response.status as LSHTTPResponseStatus.Failed
    }
  }

  /**
   * A request to a non-http(s) address can't even be constructed.
   */

  @Test
  fun testClientRequestImpossibleURI() {
    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)

    Assertions.assertThrows(IllegalArgumentException::class.java) {
      client.newRequest("urn:unusable")
        .build()
    }
  }

  /**
   * Server redirects are followed.
   */

  @Test
  fun testClientRequestRedirects() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello elsewhere.")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .addHeader("Authorization", "Basic YTpiCg==")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello elsewhere.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)
    Assertions.assertEquals("Basic YTpiCg==", request0.getHeader("Authorization"))

    val request1 = this.serverElsewhere.takeRequest()
    Assertions.assertEquals("GET", request1.method)
    Assertions.assertEquals(null, request1.getHeader("Authorization"))
  }

  /**
   * Server redirects are not followed if disabled.
   */

  @Test
  fun testClientRequestRedirectsIgnored() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello elsewhere.")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .addHeader("Authorization", "Basic YTpiCg==")
        .allowRedirects(DISALLOW_REDIRECTS)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(301, status.status)
      Assertions.assertEquals(
        this.serverElsewhere.url("/abc").toString(),
        status.header("Location")
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)
    Assertions.assertEquals("Basic YTpiCg==", request0.getHeader("Authorization"))
    Assertions.assertEquals(0, this.serverElsewhere.requestCount)
  }

  /**
   * Authorization values are sent.
   */

  @Test
  fun testClientRequestAuthorizationBasic() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setAuthorization(LSHTTPAuthorizationBasic.ofUsernamePassword("a", "b"))
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)
    Assertions.assertEquals("Basic YTpi", request0.getHeader("Authorization"))
  }

  /**
   * Authorization values are sent.
   */

  @Test
  fun testClientRequestAuthorizationBearer() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setAuthorization(LSHTTPAuthorizationBearerToken.ofToken("abcd"))
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)
    Assertions.assertEquals("Bearer abcd", request0.getHeader("Authorization"))
  }

  /**
   * Server redirects are followed for PUT.
   */

  @Test
  fun testClientRequestRedirectsPUT() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello elsewhere.")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(302)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .setMethod(Put(ByteArray(0), octetStream))
        .addHeader("Authorization", "Basic YTpiCg==")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello elsewhere.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("PUT", request0.method)
    Assertions.assertEquals("Basic YTpiCg==", request0.getHeader("Authorization"))

    val request1 = this.serverElsewhere.takeRequest()
    Assertions.assertEquals("GET", request1.method)
    Assertions.assertEquals(null, request1.getHeader("Authorization"))
  }

  /**
   * Cookies are received.
   */

  @Test
  fun testCookiesReceive() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .addHeader("Set-Cookie", "x=y; Expires=Mon, 01 Jan 2020 00:00:00 UTC;")
        .addHeader("Set-Cookie", "a=b; Secure")
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )

      Assertions.assertEquals(2, status.cookies.size)
      val cookie0 = status.cookies[0]
      Assertions.assertEquals(LocalDateTime.parse("2020-01-01T00:00:00.0Z", ISODateTimeFormat.dateTime()), cookie0.expiresAt)
      Assertions.assertEquals(false, cookie0.secure)
      Assertions.assertEquals(false, cookie0.httpOnly)
      Assertions.assertEquals("x", cookie0.name)
      Assertions.assertEquals("y", cookie0.value)

      val cookie1 = status.cookies[1]
      Assertions.assertEquals(null, cookie1.expiresAt)
      Assertions.assertEquals(true, cookie1.secure)
      Assertions.assertEquals(false, cookie1.httpOnly)
      Assertions.assertEquals("a", cookie1.name)
      Assertions.assertEquals("b", cookie1.value)
    }

    val request1 = this.server.takeRequest()
    Assertions.assertEquals("GET", request1.method)
  }

  /**
   * Cookies are received through redirects.
   */

  @Test
  fun testCookiesReceiveRedirect() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .addHeader("Set-Cookie", "x=y; Expires=Mon, 01 Jan 2020 00:00:00 UTC;")
        .addHeader("Set-Cookie", "a=b; Secure")
        .setBody("Hello.")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )

      Assertions.assertEquals(2, status.cookies.size)
      val cookie0 = status.cookies[0]
      Assertions.assertEquals(LocalDateTime.parse("2020-01-01T00:00:00.0Z", ISODateTimeFormat.dateTime()), cookie0.expiresAt)
      Assertions.assertEquals(false, cookie0.secure)
      Assertions.assertEquals(false, cookie0.httpOnly)
      Assertions.assertEquals("x", cookie0.name)
      Assertions.assertEquals("y", cookie0.value)

      val cookie1 = status.cookies[1]
      Assertions.assertEquals(null, cookie1.expiresAt)
      Assertions.assertEquals(true, cookie1.secure)
      Assertions.assertEquals(false, cookie1.httpOnly)
      Assertions.assertEquals("a", cookie1.name)
      Assertions.assertEquals("b", cookie1.value)
    }

    val request1 = this.server.takeRequest()
    Assertions.assertEquals("GET", request1.method)
  }

  /**
   * Cookies are sent.
   */

  @Test
  fun testCookiesSent() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .addCookie("x", "y")
        .addCookie("a", "b")
        .removeCookie("a")
        .removeAllCookies()
        .addCookie("c", "d")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request1 = this.server.takeRequest()
    Assertions.assertEquals("GET", request1.method)
    Assertions.assertEquals("c=d;", request1.getHeader("Cookie"))
  }

  /**
   * Multiple cookies are sent.
   */

  @Test
  fun testCookiesSentMultiple() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .addCookie("x", "y")
        .addCookie("a", "b")
        .addCookie("c", "d")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request1 = this.server.takeRequest()
    Assertions.assertEquals("GET", request1.method)
    Assertions.assertEquals("a=b;c=d;x=y;", request1.getHeader("Cookie"))
  }

  /**
   * Cookies are sent through redirects.
   */

  @Test
  fun testCookiesSentMultipleRedirect() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello.")
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
        .setBody("Hello.")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .addCookie("x", "y")
        .addCookie("a", "b")
        .addCookie("c", "d")
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)
    Assertions.assertEquals("a=b;c=d;x=y;", request0.getHeader("Cookie"))

    val request1 = this.serverElsewhere.takeRequest()
    Assertions.assertEquals("GET", request1.method)
    Assertions.assertEquals("a=b;c=d;x=y;", request1.getHeader("Cookie"))
  }

  /**
   * Downgrading from HTTPS to HTTP is not allowed.
   */

  @Test
  fun testHTTPSDangerousDowngrade0() {
    val tls =
      LSHTTPTestTLS.create()

    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello.")
    )

    this.server.useHttps(
      sslSocketFactory = tls.serverContext.socketFactory,
      tunnelProxy = false
    )

    this.configuration =
      this.configuration.copy(
        tlsOverrides = LSHTTPTLSOverrides(
          tls.clientContext.socketFactory,
          LSHTTPUnsafeTLS.unsafeTrustManager(),
          LSHTTPUnsafeTLS.unsafeHostnameVerifier()
        ))

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
        .setBody("Redirect!")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(301, status.status)
      Assertions.assertEquals(
        "Redirect!",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)

    Assertions.assertEquals(0, this.serverElsewhere.requestCount)
  }

  /**
   * Downgrading from HTTPS to HTTP is only allowed upon request.
   */

  @Test
  fun testHTTPSDangerousDowngradeOnDemand() {
    val tls =
      LSHTTPTestTLS.create()

    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("Hello.")
    )

    this.server.useHttps(
      sslSocketFactory = tls.serverContext.socketFactory,
      tunnelProxy = false
    )

    this.configuration =
      this.configuration.copy(
        tlsOverrides = LSHTTPTLSOverrides(
          tls.clientContext.socketFactory,
          LSHTTPUnsafeTLS.unsafeTrustManager(),
          LSHTTPUnsafeTLS.unsafeHostnameVerifier()
        ))

    this.server.enqueue(
      MockResponse()
        .setResponseCode(301)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
        .setBody("Redirect!")
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .allowRedirects(ALLOW_UNSAFE_REDIRECTS)
        .build()

    request.execute().use { response ->
      val status = response.status as LSHTTPResponseStatus.Responded.OK
      Assertions.assertEquals(200, status.status)
      Assertions.assertEquals(
        "Hello.",
        String(status.bodyStream?.readBytes() ?: ByteArray(0))
      )
    }

    val request0 = this.server.takeRequest()
    Assertions.assertEquals("GET", request0.method)

    val request1 = this.serverElsewhere.takeRequest()
    Assertions.assertEquals("GET", request1.method)
  }
}
