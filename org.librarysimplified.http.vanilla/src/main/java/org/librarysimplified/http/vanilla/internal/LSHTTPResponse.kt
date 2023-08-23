package org.librarysimplified.http.vanilla.internal

import okhttp3.Cookie
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import one.irradia.mime.api.MIMEType
import one.irradia.mime.vanilla.MIMEParser
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import org.librarysimplified.http.api.LSHTTPCookie
import org.librarysimplified.http.api.LSHTTPProblemReport
import org.librarysimplified.http.api.LSHTTPProblemReportParserType
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.api.LSHTTPResponseProperties
import org.librarysimplified.http.api.LSHTTPResponseStatus
import org.librarysimplified.http.api.LSHTTPResponseType

class LSHTTPResponse(
  override val status: LSHTTPResponseStatus,
  val response: Response?,
) : LSHTTPResponseType {

  companion object {

    /**
     * Translate an okHTTP response to an LS response.
     */

    fun ofOkResponse(
      client: LSHTTPClient,
      response: Response,
    ): LSHTTPResponse {
      val request = response.request
      val responseCode = response.code
      val responseMessage = response.message

      val responseContentType =
        this.parseResponseContentType(client, response)
      val responseLength =
        response.body?.contentLength()

      val responseBody = response.body
      val problemReport =
        this.parseProblemReportIfNecessary(
          client = client,
          request = request,
          responseContentType = responseContentType,
          responseBody = responseBody,
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
          client.logger.debug(
            "[{}]: problem report changed status {} -> {}",
            request.url,
            responseCode,
            status,
          )
          status
        } else {
          responseCode
        }

      val okCookies =
        Cookie.parseAll(request.url, response.headers)
      val cookies =
        okCookies.map { this.lsCookieOf(it) }

      val properties =
        LSHTTPResponseProperties(
          status = adjustedStatus,
          originalStatus = responseCode,
          contentType = responseContentType,
          contentLength = responseLength,
          problemReport = problemReport,
          message = responseMessage,
          headers = response.headers.toMultimap(),
          cookies = cookies,
          authorization = request.tag(LSHTTPRequestProperties::class.java)?.authorization,
        )

      return when {
        adjustedStatus >= 400 -> {
          LSHTTPResponse(
            status = LSHTTPResponseStatus.Responded.Error(
              properties = properties,
              bodyStream = responseStream,
            ),
            response = response,
          )
        }
        adjustedStatus >= 300 -> {
          LSHTTPResponse(
            status = LSHTTPResponseStatus.Responded.Error(
              properties = properties.copy(
                message = refusedRedirectMessage(properties.header("location")),
              ),
              bodyStream = responseStream,
            ),
            response = response,
          )
        }
        else -> {
          LSHTTPResponse(
            status = LSHTTPResponseStatus.Responded.OK(
              properties = properties,
              bodyStream = responseStream,
            ),
            response = response,
          )
        }
      }
    }

    private fun refusedRedirectMessage(location: String?): String {
      return if (location != null) {
        "Refused to follow a redirect to $location."
      } else {
        "Refused to follow a redirect."
      }
    }

    private fun lsCookieOf(
      cookie: Cookie,
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
          Pair("persistent", cookie.persistent.toString()),
        ),
      )
    }

    private fun parseProblemReportIfNecessary(
      client: LSHTTPClient,
      request: Request,
      responseContentType: MIMEType,
      responseBody: ResponseBody?,
    ): LSHTTPProblemReport? {
      val responseType = responseContentType.fullType
      val problemType = LSHTTPMimeTypes.problemReport.fullType
      return if (responseType == problemType && responseBody != null) {
        try {
          client.problemReportParsers.createParser(
            uri = request.url.toString(),
            stream = responseBody.byteStream(),
          ).use(LSHTTPProblemReportParserType::execute)
        } catch (e: Exception) {
          null
        }
      } else {
        null
      }
    }

    private fun parseResponseContentType(
      client: LSHTTPClient,
      response: Response,
    ): MIMEType {
      val contentType =
        response.header("content-type") ?: return LSHTTPMimeTypes.octetStream

      return try {
        MIMEParser.parseRaisingException(contentType)
      } catch (e: Exception) {
        client.logger.error(
          "[{}]: could not parse content type: {}: ",
          response.request.url,
          contentType,
          e,
        )
        LSHTTPMimeTypes.octetStream
      }
    }
  }

  override fun close() {
    this.response?.closeQuietly()
  }
}
