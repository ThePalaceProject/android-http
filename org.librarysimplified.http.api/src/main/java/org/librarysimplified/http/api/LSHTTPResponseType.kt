package org.librarysimplified.http.api

import java.io.Closeable

/**
 * A response to a request. Responses should be closed to ensure the timely disposal of
 * any resources.
 */

interface LSHTTPResponseType : Closeable {
  /**
   * The properties of the response.
   */

  val properties: LSHTTPResponseProperties?
    get() = this.status.properties

  /**
   * The response status.
   */

  val status: LSHTTPResponseStatus
}
