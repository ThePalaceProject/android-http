package org.librarysimplified.http.tests.downloads

import android.content.Context
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.downloads.LSHTTPDownloadRequest
import org.librarysimplified.http.downloads.LSHTTPDownloadState
import org.librarysimplified.http.downloads.LSHTTPDownloadState.DownloadReceiving
import org.librarysimplified.http.downloads.LSHTTPDownloadState.DownloadStarted
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadCancelled
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadCompletedSuccessfully
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedExceptionally
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedServer
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedUnacceptableMIME
import org.librarysimplified.http.downloads.LSHTTPDownloads
import org.librarysimplified.http.tests.LSHTTPTestDirectories
import org.librarysimplified.http.vanilla.LSHTTPProblemReportParsers
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File

abstract class LSHTTPDownloadsContract {

  private val logger =
    LoggerFactory.getLogger(LSHTTPDownloadsContract::class.java)

  private lateinit var eventLog: MutableList<LSHTTPDownloadState>
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
    this.eventLog = mutableListOf()

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

  private fun logEvent(event: LSHTTPDownloadState) {
    this.logger.debug("event: {}", event)
    this.eventLog.add(event)
  }

  /**
   * A 404 error fails the download.
   */

  @Test
  fun testFailed404() {
    this.server.enqueue(MockResponse().setResponseCode(404))

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { false },
      )

    val result = LSHTTPDownloads.download(downloadRequest) as DownloadFailed
    assertEquals(404, (result.responseStatus as LSHTTPResponseStatus.Responded).properties.status)
    assertFalse(outputFile.exists())

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadFailedServer::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * A timeout fails the download.
   */

  @Test
  fun testFailedError() {
    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { false },
      )

    val result = LSHTTPDownloads.download(downloadRequest) as DownloadFailedExceptionally
    assertEquals(LSHTTPResponseStatus.Failed::class.java, result.responseStatus!!.javaClass)
    assertFalse(outputFile.exists())

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadFailedExceptionally::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * Cancellation cancels the download.
   */

  @Test
  fun testCancellation() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("Hello!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { true },
      )

    LSHTTPDownloads.download(downloadRequest) as DownloadCancelled
    assertFalse(outputFile.exists())

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadCancelled::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * Downloading works.
   */

  @Test
  fun testDownloadOK() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("Hello!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { false },
      )

    val result = LSHTTPDownloads.download(downloadRequest) as DownloadCompletedSuccessfully
    assertTrue(outputFile.isFile)
    assertEquals("Hello!", outputFile.readText())
    assertEquals(6L, result.receivedSize)

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadReceiving::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadReceiving::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadCompletedSuccessfully::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * Downloading fails if the content type isn't acceptable.
   */

  @Test
  fun testDownloadUnacceptableType() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("Hello!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isMIMETypeAcceptable = { false },
        isCancelled = { false },
      )

    LSHTTPDownloads.download(downloadRequest) as DownloadFailedUnacceptableMIME
    assertFalse(outputFile.exists())

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadFailedUnacceptableMIME::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * Downloading does not fail the file size check if the content length is unknown (the response
   * is chunked).
   */

  @Test
  fun testDownloadUnknownContentLength() {
    val body = "hello world"
    val bodyInputStream = ByteArrayInputStream(body.toByteArray())
    val buffer = Buffer().apply { readFrom(bodyInputStream) }

    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setChunkedBody(buffer, 8),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { false },
      )

    val result = LSHTTPDownloads.download(downloadRequest) as DownloadCompletedSuccessfully
    assertTrue(outputFile.isFile)
    assertEquals("hello world", outputFile.readText())
    assertEquals(11L, result.receivedSize)
  }

  /**
   * Downloading fails if the provided functions raise exceptions.
   */

  @Test
  fun testDownloadExceptional() {
    this.server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("Hello!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        outputFile = outputFile,
        onEvent = this::logEvent,
        isMIMETypeAcceptable = {
          throw IllegalArgumentException()
        },
        isCancelled = { false },
      )

    LSHTTPDownloads.download(downloadRequest) as DownloadFailedExceptionally
    assertFalse(outputFile.exists())

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadFailedExceptionally::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }

  /**
   * Downloading through a 302 redirect works.
   */

  @Test
  fun testDownload302() {
    this.serverElsewhere.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "text/plain")
        .setBody("Hello!"),
    )

    this.server.enqueue(
      MockResponse()
        .setResponseCode(302)
        .setHeader("Location", this.serverElsewhere.url("/abc"))
        .setHeader("content-type", "text/html")
        .setBody("Hello!"),
    )

    val clients = this.clients()
    val client = clients.create(this.context, this.configuration)
    val request =
      client.newRequest(this.server.url("/xyz").toString())
        .build()

    val outputFile =
      File(this.directory, "output.txt")

    val downloadRequest =
      LSHTTPDownloadRequest(
        request = request,
        isMIMETypeAcceptable = {
            type ->
          type.fullType == "text/plain"
        },
        outputFile = outputFile,
        onEvent = this::logEvent,
        isCancelled = { false },
      )

    val result = LSHTTPDownloads.download(downloadRequest) as DownloadCompletedSuccessfully
    assertTrue(outputFile.isFile)
    assertEquals("Hello!", outputFile.readText())
    assertEquals(6L, result.receivedSize)

    assertEquals(DownloadStarted::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadReceiving::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadReceiving::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(DownloadCompletedSuccessfully::class.java, this.eventLog.removeAt(0).javaClass)
    assertEquals(0, this.eventLog.size)
  }
}
