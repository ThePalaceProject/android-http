package org.librarysimplified.http.vanilla.internal

import okhttp3.Cookie
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import one.irradia.mime.api.MIMEType
import one.irradia.mime.vanilla.MIMEParser
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import org.librarysimplified.http.api.LSHTTPCookie
import org.librarysimplified.http.api.LSHTTPProblemReport
import org.librarysimplified.http.api.LSHTTPProblemReportParserType
import org.librarysimplified.http.api.LSHTTPRequestBuilderType.AllowRedirects
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.api.LSHTTPRequestType
import org.librarysimplified.http.api.LSHTTPResponseStatus

class LSHTTPRequest(
  private val client: LSHTTPClient,
  private val allowRedirects: AllowRedirects,
  override val properties: LSHTTPRequestProperties,
  private val modifier: (LSHTTPRequestProperties) -> LSHTTPRequestProperties
) : LSHTTPRequestType {

  private lateinit var request: Request

  override fun execute(): LSHTTPResponse {
    this.request =
      LSOKHTTPRequests.createRequest(this.properties)

    try {
      this.client.logger.debug(
        "[{}] creating client with {}",
        this.request.url,
        this.allowRedirects
      )

      val okClient =
        this.client.createOkClient(this.allowRedirects, this.modifier)
      val call =
        okClient.newCall(this.request)

      val response = call.execute()
      val responseCode = response.code
      val responseMessage = response.message

      val responseContentType =
        this.parseResponseContentType(response)
      val responseLength =
        response.body?.contentLength()

      this.client.logger.debug(
        "[{}] <- {} {} ({} octets, {})",
        this.request.url,
        responseCode,
        responseMessage,
        responseLength,
        responseContentType
      )

      val responseBody = response.body
      val problemReport =
        this.parseProblemReportIfNecessary(
          responseContentType = responseContentType,
          responseBody = responseBody
        )

      val responseStream =
        if (problemReport == null) {
          responseBody?.byteStream()
        } else {
          null
        }

      val adjustedStatus =
        if (problemReport?.status != null) {
          val status = problemReport.status!!
          this.client.logger.debug(
            "[{}]: problem report changed status {} -> {}",
            this.request.url,
            responseCode,
            status
          )
          status
        } else {
          responseCode
        }

      val okCookies =
        Cookie.parseAll(this.request.url, response.headers)
      val cookies =
        okCookies.map { this.lsCookieOf(it) }

      return if (adjustedStatus >= 400) {
        LSHTTPResponse(
          this,
          response = response,
          status = LSHTTPResponseStatus.Responded.Error(
            status = adjustedStatus,
            originalStatus = responseCode,
            contentType = responseContentType,
            contentLength = responseLength,
            problemReport = problemReport,
            message = responseMessage,
            bodyStream = responseStream,
            headers = response.headers.toMultimap(),
            cookies = cookies
          )
        )
      } else {
        LSHTTPResponse(
          this,
          response = response,
          status = LSHTTPResponseStatus.Responded.OK(
            status = adjustedStatus,
            originalStatus = responseCode,
            contentType = responseContentType,
            contentLength = responseLength,
            problemReport = problemReport,
            message = responseMessage,
            bodyStream = responseStream,
            headers = response.headers.toMultimap(),
            cookies = cookies
          )
        )
      }
    } catch (e: Exception) {
      this.client.logger.error(
        "[{}]: request failed: ",
        this.request.url,
        e
      )
      return LSHTTPResponse(
        request = this,
        response = null,
        status = LSHTTPResponseStatus.Failed(e)
      )
    }
  }

  private fun lsCookieOf(
    cookie: Cookie
  ): LSHTTPCookie {

    /*
     * Apparently, anything after December 31, 9999 means "does not expire".
     */

    val expiryBound =
      LocalDateTime.parse("9999-12-31T00:00:00.0Z", ISODateTimeFormat.dateTime())
    val expiryGiven =
      LocalDateTime(cookie.expiresAt)
    val expires =
      if (expiryGiven.isAfter(expiryBound)) {
        null
      } else {
        expiryGiven
      }

    return LSHTTPCookie(
      name = cookie.name,
      value = cookie.value,
      secure = cookie.secure,
      httpOnly = cookie.httpOnly,
      expiresAt = expires,
      attributes = mapOf(
        Pair("domain", cookie.domain),
        Pair("hostOnly", cookie.hostOnly.toString()),
        Pair("persistent", cookie.persistent.toString())
      )
    )
  }

  private fun parseProblemReportIfNecessary(
    responseContentType: MIMEType,
    responseBody: ResponseBody?
  ): LSHTTPProblemReport? {
    val responseType = responseContentType.fullType
    val problemType = LSHTTPMimeTypes.problemReport.fullType
    return if (responseType == problemType && responseBody != null) {
      try {
        this.client.problemReportParsers.createParser(
          uri = this.request.url.toString(),
          stream = responseBody.byteStream()
        ).use(LSHTTPProblemReportParserType::execute)
      } catch (e: Exception) {
        null
      }
    } else {
      null
    }
  }

  private fun parseResponseContentType(
    response: Response
  ): MIMEType {
    val contentType =
      response.header("content-type") ?: return LSHTTPMimeTypes.octetStream

    return try {
      MIMEParser.parseRaisingException(contentType)
    } catch (e: Exception) {
      this.client.logger.error(
        "[{}]: could not parse content type: {}: ", this.request.url, contentType, e
      )
      LSHTTPMimeTypes.octetStream
    }
  }
}
