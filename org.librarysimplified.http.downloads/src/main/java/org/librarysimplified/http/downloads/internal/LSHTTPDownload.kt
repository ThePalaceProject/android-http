package org.librarysimplified.http.downloads.internal

import org.librarysimplified.http.api.LSHTTPRequestConstants
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.downloads.LSHTTPDownloadRequest
import org.librarysimplified.http.downloads.LSHTTPDownloadState.DownloadReceiving
import org.librarysimplified.http.downloads.LSHTTPDownloadState.DownloadStarted
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadCancelled
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadCompletedSuccessfully
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedExceptionally
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedServer
import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult.DownloadFailed.DownloadFailedUnacceptableMIME
import org.librarysimplified.http.downloads.LSHTTPDownloadType
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CancellationException

class LSHTTPDownload(
  private val request: LSHTTPDownloadRequest,
) : LSHTTPDownloadType {

  private val logger =
    LoggerFactory.getLogger(LSHTTPDownload::class.java)

  private class UnacceptableMIME(
    val status: LSHTTPResponseStatus.Responded.OK,
  ) : Exception()

  override fun execute(): LSHTTPDownloadResult {
    return try {
      this.request.onEvent(DownloadStarted)
      this.request.request.execute().use { response ->
        when (val status = response.status) {
          is LSHTTPResponseStatus.Responded.OK ->
            this.handleTransfer(status)
          is LSHTTPResponseStatus.Responded.Error ->
            this.handleRespondedError(status)
          is LSHTTPResponseStatus.Failed ->
            this.handleFailed(status)
        }
      }
    } catch (e: UnacceptableMIME) {
      val state = DownloadFailedUnacceptableMIME(e.status, e)
      this.request.onEvent(state)
      state
    } catch (e: CancellationException) {
      this.request.onEvent(DownloadCancelled)
      DownloadCancelled
    } catch (e: java.lang.Exception) {
      this.logger.error("unexpected exception: ", e)
      val state = DownloadFailedExceptionally(LSHTTPResponseStatus.Failed(e), e)
      this.request.onEvent(state)
      state
    }
  }

  private fun handleFailed(
    status: LSHTTPResponseStatus.Failed,
  ): LSHTTPDownloadResult {
    val result = DownloadFailedExceptionally(
      responseStatus = status,
      exception = status.exception,
    )
    this.request.onEvent(result)
    return result
  }

  private fun handleRespondedError(
    status: LSHTTPResponseStatus.Responded.Error,
  ): LSHTTPDownloadResult {
    val result = DownloadFailedServer(status)
    this.request.onEvent(result)
    return result
  }

  private fun handleTransfer(
    status: LSHTTPResponseStatus.Responded.OK,
  ): LSHTTPDownloadResult {
    this.checkMIME(status)
    this.checkCancellation()

    val inputStream =
      status.bodyStream ?: ByteArrayInputStream(ByteArray(0))

    return this.transfer(
      status = status,
      expectedSize = status.properties.contentLength,
      inputStream = inputStream,
    )
  }

  private fun checkMIME(status: LSHTTPResponseStatus.Responded.OK) {
    if (!this.request.isMIMETypeAcceptable(status.properties.contentType)) {
      throw UnacceptableMIME(status)
    }
  }

  private fun transfer(
    status: LSHTTPResponseStatus,
    expectedSize: Long?,
    inputStream: InputStream,
  ): LSHTTPDownloadResult {
    return this.request.outputFile.outputStream().use { outputStream ->
      val unitsPerSecond = LSHTTPDownloadUnitsPerSecond(this.request.clock)
      val buffer = ByteArray(65536)
      var total = 0L

      this.request.onEvent(
        DownloadReceiving(
          expectedSize = expectedSize,
          receivedSize = total,
          bytesPerSecond = 0L,
          accessToken = status.properties?.header(LSHTTPRequestConstants.PROPERTY_KEY_ACCESS_TOKEN),
        )
      )

      while (true) {
        this.checkCancellation()

        val read = inputStream.read(buffer)
        if (read == -1) {
          break
        }
        outputStream.write(buffer, 0, read)
        total += read
        if (unitsPerSecond.update(read.toLong())) {
          this.request.onEvent(
            DownloadReceiving(
              expectedSize = expectedSize,
              receivedSize = total,
              bytesPerSecond = unitsPerSecond.now,
              accessToken =
                status.properties?.header(LSHTTPRequestConstants.PROPERTY_KEY_ACCESS_TOKEN),
            )
          )
        }
      }

      outputStream.flush()
      this.checkOutputFile(expectedSize, total)

      val result =
        DownloadCompletedSuccessfully(
          receivedSize = total,
          responseStatus = status,
        )

      this.request.onEvent(result)
      result
    }
  }

  private fun checkOutputFile(
    expectedSize: Long?,
    total: Long,
  ) {
    val fileLength = this.request.outputFile.length()
    if (expectedSize != null && expectedSize >= 0) {
      check(fileLength == expectedSize) {
        "Output file size $fileLength must match expected size $expectedSize"
      }
    }
    check(fileLength == total) {
      "Output file size $fileLength must match received size $total"
    }
  }

  private fun checkCancellation() {
    if (this.request.isCancelled()) {
      throw CancellationException()
    }
  }
}
