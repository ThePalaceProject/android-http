package org.librarysimplified.http.api

import java.io.InputStream

/**
 * The type of response status values. A response can be one of three types:
 *
 * 1. The request couldn't be sent due to a network or other I/O error.
 * 2. The request was sent and the server responded with an error code.
 * 3. The request was sent and the server responded with a successful status code.
 */

sealed class LSHTTPResponseStatus {

  /**
   * The properties of the response.
   */

  abstract val properties: LSHTTPResponseProperties?

  /**
   * The type of responses that indicate the server returned something.
   */

  sealed class Responded : LSHTTPResponseStatus() {

    abstract override val properties: LSHTTPResponseProperties

    /**
     * The stream of data returned as the HTTP response body
     */

    abstract val bodyStream: InputStream?

    /**
     * The server responded with a successful status code.
     */

    data class OK(
      override val properties: LSHTTPResponseProperties,
      override val bodyStream: InputStream?,
    ) : Responded()

    /**
     * The server responded with an error code.
     */

    data class Error(
      override val properties: LSHTTPResponseProperties,
      override val bodyStream: InputStream?,
    ) : Responded()
  }

  /**
   * The request failed to yield a response due to the given exception.
   */

  data class Failed(
    val exception: Exception,
  ) : LSHTTPResponseStatus() {
    override val properties: LSHTTPResponseProperties? =
      null
  }
}
