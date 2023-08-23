package org.librarysimplified.http.tests.bearer_token

import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.bearer_token.LSHTTPBearerTokenInterceptors
import org.librarysimplified.http.vanilla.LSHTTPClients

class LSHTTPBearerTokenTest : LSHTTPBearerTokenContract() {
  override fun clients(parsers: LSHTTPProblemReportParserFactoryType): LSHTTPClientProviderType {
    return LSHTTPClients(
      parsers,
      listOf(LSHTTPBearerTokenInterceptors()),
    )
  }
}
