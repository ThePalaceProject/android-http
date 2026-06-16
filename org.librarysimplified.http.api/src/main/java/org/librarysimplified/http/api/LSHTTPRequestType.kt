package org.librarysimplified.http.api

/**
 * An immutable request.
 */

interface LSHTTPRequestType {
  /**
   * The properties of the request
   */

  val properties: LSHTTPRequestProperties

  /**
   * Execute the request on the client that created it.
   */

  fun execute(): LSHTTPResponseType
}
