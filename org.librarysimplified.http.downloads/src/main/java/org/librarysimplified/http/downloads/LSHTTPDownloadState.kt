package org.librarysimplified.http.downloads

import org.librarysimplified.http.api.LSHTTPResponseStatus

/**
 * The state of a given download.
 */

sealed class LSHTTPDownloadState {

  /**
   * The download has started. Implementations must guarantee to publish this state exactly
   * once at the start of a download.
   */

  object DownloadStarted : LSHTTPDownloadState()

  /**
   * The download is running and has received data. Implementations must guarantee to rate-limit
   * the publication of this state in order to avoid overloading consumers with thousands of
   * events.
   */

  data class DownloadReceiving(
    val expectedSize: Long?,
    val receivedSize: Long,
    val bytesPerSecond: Long,
    val accessToken: String?,
  ) : LSHTTPDownloadState()

  /**
   * The subset of download states that indicate end states.
   */

  sealed class LSHTTPDownloadResult : LSHTTPDownloadState() {

    /**
     * The response the server sent, if any.
     */

    abstract val responseStatus: LSHTTPResponseStatus?

    /**
     * Downloading was cancelled explicitly.
     *
     * @see [LSHTTPDownloadRequest.isCancelled]
     */

    object DownloadCancelled : LSHTTPDownloadResult() {
      override val responseStatus: LSHTTPResponseStatus? =
        null
    }

    /**
     * The download failed.
     */

    sealed class DownloadFailed : LSHTTPDownloadResult() {

      /**
       * The download failed because the server sent a failure response.
       */

      data class DownloadFailedServer(
        override val responseStatus: LSHTTPResponseStatus.Responded,
      ) : DownloadFailed()

      /**
       * The download failed because the user said the MIME type the server returned was
       * unacceptable.
       *
       * @see [LSHTTPDownloadRequest.isMIMETypeAcceptable]
       */

      data class DownloadFailedUnacceptableMIME(
        override val responseStatus: LSHTTPResponseStatus,
        val exception: Throwable,
      ) : DownloadFailed()

      /**
       * The download failed because the code raised an exception.
       */

      data class DownloadFailedExceptionally(
        override val responseStatus: LSHTTPResponseStatus?,
        val exception: Throwable,
      ) : DownloadFailed()
    }

    /**
     * The download completed successfully.
     */

    data class DownloadCompletedSuccessfully(
      override val responseStatus: LSHTTPResponseStatus,
      val receivedSize: Long,
    ) : LSHTTPDownloadResult()
  }
}
