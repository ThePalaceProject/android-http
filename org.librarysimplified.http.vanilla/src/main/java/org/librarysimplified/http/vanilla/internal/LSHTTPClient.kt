package org.librarysimplified.http.vanilla.internal

import android.content.Context
import okhttp3.OkHttpClient
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPRequestBuilderType
import org.librarysimplified.http.api.LSHTTPRequestProperties
import org.librarysimplified.http.api.LSHTTPResponseType
import org.librarysimplified.http.vanilla.BuildConfig
import org.librarysimplified.http.vanilla.extensions.LSHTTPInterceptorFactoryType
import org.slf4j.LoggerFactory
import java.net.URI

class LSHTTPClient(
  val context: Context,
  val configuration: LSHTTPClientConfiguration,
  val problemReportParsers: LSHTTPProblemReportParserFactoryType,
  val interceptors: List<LSHTTPInterceptorFactoryType>,
) : LSHTTPClientType {

  internal val logger =
    LoggerFactory.getLogger(LSHTTPClient::class.java)

  override fun newRequest(url: URI): LSHTTPRequestBuilderType {
    val requestBuilder = LSHTTPRequestBuilder(this, url)
    requestBuilder.addHeader("User-Agent", this.userAgent())
    return requestBuilder
  }

  override fun userAgent(): String {
    return "${this.configuration.applicationName}/${this.configuration.applicationVersion} (Simplified-Android-HTTP ${BuildConfig.HTTP_VERSION_NAME})"
  }

  internal fun createOkClient(
    redirects: LSHTTPRequestBuilderType.AllowRedirects,
    modifier: ((LSHTTPRequestProperties) -> LSHTTPRequestProperties)?,
    observer: ((LSHTTPResponseType) -> Unit)?,
  ): OkHttpClient {
    val builder = OkHttpClient.Builder()

    if (modifier != null) {
      builder.addNetworkInterceptor(LSHTTPRedirectRequestInterceptor(modifier))
    }
    if (observer != null) {
      builder.addNetworkInterceptor(LSHTTPRedirectResponseInterceptor(this, observer))
    }

    builder.addNetworkInterceptor(LSHTTPLoggingInterceptor(this.logger))

    val timeout = this.configuration.timeout
    builder.callTimeout(timeout.first, timeout.second)
    builder.connectTimeout(timeout.first, timeout.second)
    builder.readTimeout(timeout.first, timeout.second)
    builder.writeTimeout(timeout.first, timeout.second)

    when (redirects) {
      LSHTTPRequestBuilderType.AllowRedirects.ALLOW_REDIRECTS -> {
        builder.followRedirects(true)
        builder.followSslRedirects(false)
      }
      LSHTTPRequestBuilderType.AllowRedirects.DISALLOW_REDIRECTS -> {
        builder.followRedirects(false)
        builder.followSslRedirects(false)
      }
      LSHTTPRequestBuilderType.AllowRedirects.ALLOW_UNSAFE_REDIRECTS -> {
        builder.followRedirects(true)
        builder.followSslRedirects(true)
      }
    }

    this.logger.debug("{} available client interceptor extensions", this.interceptors.size)
    for (index in this.interceptors.indices) {
      val interceptorFactory = this.interceptors[index]
      this.logger.debug("[{}] interceptor {} {}", index, interceptorFactory.name, interceptorFactory.version)
      val interceptor = interceptorFactory.createInterceptor(this.context)
      builder.addInterceptor(interceptor)
    }

    val tlsOverrides = this.configuration.tlsOverrides
    if (tlsOverrides != null) {
      builder.sslSocketFactory(
        sslSocketFactory = tlsOverrides.sslSocketFactory,
        trustManager = tlsOverrides.trustManager,
      )
      builder.hostnameVerifier(tlsOverrides.hostnameVerifier)
    }

    return builder.build()
  }
}
