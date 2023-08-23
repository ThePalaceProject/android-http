package org.librarysimplified.http.vanilla.internal

import okhttp3.Request
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.AllowRedirects
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.api.LSHTTPRequestType
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.api.LSHTTPResponseType

class LSHTTPRequest(
  private val client: LSHTTPClient,
  private val allowRedirects: AllowRedirects,
  override val properties: LSHTTPRequestProperties,
  private val modifier: ((LSHTTPRequestProperties) -> LSHTTPRequestProperties)?,
  private val observer: ((LSHTTPResponseType) -> Unit)?,
) : LSHTTPRequestType {

  private lateinit var request: Request

  override fun execute(): LSHTTPResponse {
    this.request =
      LSOKHTTPRequests.createRequest(this.properties)

    try {
      this.client.logger.debug(
        "[{}] creating client with {}",
        this.request.url,
        this.allowRedirects,
      )

      val okClient =
        this.client.createOkClient(
          redirects = this.allowRedirects,
          modifier = this.modifier,
          observer = this.observer,
        )
      val call = okClient.newCall(this.request)
      val response = call.execute()
      return LSHTTPResponse.ofOkResponse(this.client, response)
    } catch (e: Exception) {
      this.client.logger.error(
        "[{}]: request failed: ",
        this.request.url,
        e,
      )
      return LSHTTPResponse(
        status = LSHTTPResponseStatus.Failed(e),
        response = null,
      )
    }
  }
}
