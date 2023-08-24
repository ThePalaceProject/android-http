package org.librarysimplified.http.vanilla.extensions

import android.content.Context
import okhttp3.Interceptor

/**
 * An interceptor factory for okhttp interceptors.
 */

interface LSHTTPInterceptorFactoryType {

  /**
   * The name of the interceptor.
   */

  val name: String

  /**
   * The version of the interceptor.
   */

  val version: String

  /**
   * Create an interceptor.
   */

  fun createInterceptor(
    context: Context,
  ): Interceptor
}
