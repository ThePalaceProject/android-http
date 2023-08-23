package org.librarysimplified.http.api

import android.content.Context

/**
 * A provider of HTTP clients.
 */

interface LSHTTPClientProviderType {

  /**
   * Create a new client using the given application context and configuration.
   */

  fun create(
    context: Context,
    configuration: LSHTTPClientConfiguration,
  ): LSHTTPClientType
}
