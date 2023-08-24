package org.librarysimplified.http.refresh_token

import android.content.Context
import okhttp3.Interceptor
import org.librarysimplified.http.refresh_token.internal.LSHTTPRefreshTokenInterceptor
import org.librarysimplified.http.vanilla.extensions.LSHTTPInterceptorFactoryType

/**
 * An interceptor that can determine when a token needs to be refresh.
 **/

class LSHTTPRefreshTokenInterceptors : LSHTTPInterceptorFactoryType {

  override val name: String =
    "org.librarysimplified.http.refresh_token"

  override val version: String =
    BuildConfig.REFRESH_TOKEN_VERSION_NAME

  override fun createInterceptor(context: Context): Interceptor {
    return LSHTTPRefreshTokenInterceptor()
  }
}
