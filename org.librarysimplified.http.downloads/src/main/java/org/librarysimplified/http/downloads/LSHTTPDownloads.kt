package org.librarysimplified.http.downloads

import org.librarysimplified.http.downloads.LSHTTPDownloadState.LSHTTPDownloadResult
import org.librarysimplified.http.downloads.internal.LSHTTPDownload

/**
 * Functions to create and start downloads.
 */

object LSHTTPDownloads {

  /**
   * Create and execute a download for the given request.
   */

  fun download(
    request: LSHTTPDownloadRequest,
  ): LSHTTPDownloadResult {
    return this.create(request).execute()
  }

  /**
   * Create a download for the given request. The download will not start running until the
   * user calls [LSHTTPDownloadType.execute].
   */

  fun create(
    request: LSHTTPDownloadRequest,
  ): LSHTTPDownloadType {
    return LSHTTPDownload(request)
  }
}
