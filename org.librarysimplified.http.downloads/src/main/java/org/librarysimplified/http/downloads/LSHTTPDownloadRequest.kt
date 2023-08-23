package org.librarysimplified.http.downloads

import one.irradia.mime.api.MIMEType
import org.joda.time.Instant
import org.librarysimplified.http.api.LSHTTPRequestType
import java.io.File

/**
 * A request to download and save a file.
 */

data class LSHTTPDownloadRequest(

  /**
   * The actual request for the file.
   */

  val request: LSHTTPRequestType,

  /**
   * The output file to which data will be written.
   */

  val outputFile: File,

  /**
   * A function that will receive events whenever the state of a download changes.
   */

  val onEvent: (LSHTTPDownloadState) -> Unit,

  /**
   * A function that will be presented with the MIME type that the server has returned
   * for the requested file. This function should return `false` if the MIME type is not what
   * the requesting code expected.
   */

  val isMIMETypeAcceptable: (MIMEType) -> Boolean = {
    true
  },

  /**
   * A function that will be consulted repeatedly and should return `true` if the user wishes
   * to cancel the download.
   */

  val isCancelled: () -> Boolean,

  /**
   * A function that returns the current time each time it is executed.
   */

  val clock: () -> Instant = {
    Instant.now()
  },
)
