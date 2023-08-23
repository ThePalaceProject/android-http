package org.librarysimplified.http.vanilla

import android.content.Context
import org.librarysimplified.http.api.LSHTTPClientConfiguration
import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPClientType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.vanilla.extensions.LSHTTPInterceptorFactoryType
import org.librarysimplified.http.vanilla.internal.LSHTTPClient
import java.util.ServiceLoader

/**
 * A provider of okhttp clients.
 */

class LSHTTPClients(
  private val problemReportParsers: LSHTTPProblemReportParserFactoryType,
  private val interceptors: List<LSHTTPInterceptorFactoryType>,
) : LSHTTPClientProviderType {

  constructor() : this(
    problemReportParsers = LSHTTPProblemReportParsers(),
    interceptors = ServiceLoader.load(LSHTTPInterceptorFactoryType::class.java).toList(),
  )

  override fun create(
    context: Context,
    configuration: LSHTTPClientConfiguration,
  ): LSHTTPClientType {
    return LSHTTPClient(
      context = context,
      problemReportParsers = problemReportParsers,
      interceptors = interceptors,
      configuration = configuration,
    )
  }
}
