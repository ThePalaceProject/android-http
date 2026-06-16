package org.librarysimplified.http.chucker

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import org.librarysimplified.http.vanilla.extensions.LSHTTPInterceptorFactoryType

/**
 * A Chucker interceptor service.
 *
 * @see "https://github.com/ChuckerTeam/chucker"
 */

class LSHTTPChuckerInterceptors : LSHTTPInterceptorFactoryType {
  override val name: String =
    "org.librarysimplified.http.chucker"
  override val version: String =
    BuildConfig.CHUCKER_VERSION_NAME

  override fun createInterceptor(context: Context): Interceptor {
    return ChuckerInterceptor(context)
  }
}
