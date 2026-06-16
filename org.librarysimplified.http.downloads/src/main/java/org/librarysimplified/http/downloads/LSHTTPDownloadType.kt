package org.librarysimplified.http.downloads

import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult

/**
 * A download to be executed.
 */

interface LSHTTPDownloadType {
  /**
   * Execute the download request.
   */

  fun execute(): LSHTTPDownloadResult
}
