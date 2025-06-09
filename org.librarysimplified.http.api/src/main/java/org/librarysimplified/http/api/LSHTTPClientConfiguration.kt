package org.librarysimplified.http.api

import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * The basic client configuration.
 */

data class LSHTTPClientConfiguration(

  /**
   * The application name (for User-Agent strings).
   */

  val applicationName: String,

  /**
   * The application version (for User-Agent strings).
   */

  val applicationVersion: String,

  /**
   * Overrides for TLS-related classes. Typically only useful for unit testing.
   */

  val tlsOverrides: LSHTTPTLSOverrides? = null,

  /**
   * The timeout used for all I/O operations (connects, reads, writes, etc).
   */

  val timeout: Pair<Long, TimeUnit> = Pair(1L, TimeUnit.MINUTES),

  /**
   * An optional proxy.
   */

  val proxy: Proxy = Proxy.NO_PROXY,
)
