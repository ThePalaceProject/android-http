package org.librarysimplified.http.vanilla.internal

import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.HttpUrl
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

class LSHTTPTimingEventListener(
  private val id: Long,
) : EventListener() {

  private val logger =
    LoggerFactory.getLogger(LSHTTPTimingEventListener::class.java)

  private var timeStart =
    System.currentTimeMillis()

  private fun event(
    eventName: String,
    location: HttpUrl,
    exception: Exception? = null,
  ) {
    val locationText =
      location.toString()
    val shortURL =
      if (locationText.length > 32) {
        locationText.substring(0, 31) + "…"
      } else {
        locationText
      }

    val timeThen = this.timeStart
    val timeNow = System.currentTimeMillis()
    this.logger.trace(
      "[0x{}] [{}] {}ms {}",
      String.format("%x", this.id),
      shortURL,
      timeNow - timeThen,
      eventName,
    )

    if (exception != null) {
      this.logger.trace(
        "[0x{}] [{}] {} exception: ",
        String.format("%x", this.id),
        shortURL,
        eventName,
        exception,
      )
    }
  }

  override fun cacheConditionalHit(
    call: Call,
    cachedResponse: Response,
  ) {
    this.event("cacheConditionalHit", cachedResponse.request.url)
  }

  override fun cacheHit(
    call: Call,
    response: Response,
  ) {
    this.event("cacheHit", response.request.url)
  }

  override fun cacheMiss(
    call: Call,
  ) {
    this.event("cacheMiss", call.request().url)
  }

  override fun callEnd(
    call: Call,
  ) {
    this.event("callEnd", call.request().url)
  }

  override fun callFailed(
    call: Call,
    ioe: IOException,
  ) {
    this.event("callFailed", call.request().url, ioe)
  }

  override fun callStart(
    call: Call,
  ) {
    this.event("callStart", call.request().url)
  }

  override fun canceled(
    call: Call,
  ) {
    this.event("canceled", call.request().url)
  }

  override fun connectEnd(
    call: Call,
    inetSocketAddress: InetSocketAddress,
    proxy: Proxy,
    protocol: Protocol?,
  ) {
    this.event("connectEnd", call.request().url)
  }

  override fun connectFailed(
    call: Call,
    inetSocketAddress: InetSocketAddress,
    proxy: Proxy,
    protocol: Protocol?,
    ioe: IOException,
  ) {
    this.event("connectFailed", call.request().url, ioe)
  }

  override fun connectStart(
    call: Call,
    inetSocketAddress: InetSocketAddress,
    proxy: Proxy,
  ) {
    this.event("connectStart", call.request().url)
  }

  override fun connectionAcquired(
    call: Call,
    connection: Connection,
  ) {
    this.event("connectionAcquired", call.request().url)
  }

  override fun connectionReleased(
    call: Call,
    connection: Connection,
  ) {
    this.event("connectionReleased", call.request().url)
  }

  override fun dnsEnd(
    call: Call,
    domainName: String,
    inetAddressList: List<InetAddress>,
  ) {
    this.event("dnsEnd", call.request().url)
  }

  override fun dnsStart(
    call: Call,
    domainName: String,
  ) {
    this.event("dnsStart", call.request().url)
  }

  override fun proxySelectEnd(
    call: Call,
    url: HttpUrl,
    proxies: List<Proxy>,
  ) {
    this.event("proxySelectEnd", url)
  }

  override fun proxySelectStart(
    call: Call,
    url: HttpUrl,
  ) {
    this.event("proxySelectStart", url)
  }

  override fun requestBodyEnd(
    call: Call,
    byteCount: Long,
  ) {
    this.event("requestBodyEnd", call.request().url)
  }

  override fun requestBodyStart(
    call: Call,
  ) {
    this.event("requestBodyStart", call.request().url)
  }

  override fun requestFailed(
    call: Call,
    ioe: IOException,
  ) {
    this.event("requestFailed", call.request().url, ioe)
  }

  override fun requestHeadersEnd(
    call: Call,
    request: Request,
  ) {
    this.event("requestHeadersEnd", call.request().url)
  }

  override fun requestHeadersStart(
    call: Call,
  ) {
    this.event("requestHeadersStart", call.request().url)
  }

  override fun responseBodyEnd(
    call: Call,
    byteCount: Long,
  ) {
    this.event("responseBodyEnd", call.request().url)
  }

  override fun responseBodyStart(
    call: Call,
  ) {
    this.event("responseBodyStart", call.request().url)
  }

  override fun responseFailed(
    call: Call,
    ioe: IOException,
  ) {
    this.event("responseFailed", call.request().url, ioe)
  }

  override fun responseHeadersEnd(
    call: Call,
    response: Response,
  ) {
    this.event("responseHeadersEnd", call.request().url)
  }

  override fun responseHeadersStart(
    call: Call,
  ) {
    this.event("responseHeadersStart", call.request().url)
  }

  override fun satisfactionFailure(
    call: Call,
    response: Response,
  ) {
    this.event("satisfactionFailure", call.request().url)
  }

  override fun secureConnectEnd(
    call: Call,
    handshake: Handshake?,
  ) {
    this.event("secureConnectEnd", call.request().url)
  }

  override fun secureConnectStart(
    call: Call,
  ) {
    this.event("secureConnectStart", call.request().url)
  }
}
