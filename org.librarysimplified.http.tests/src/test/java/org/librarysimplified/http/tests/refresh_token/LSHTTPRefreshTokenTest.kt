package org.librarysimplified.http.tests.refresh_token

import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.refresh_token.LSHTTPRefreshTokenInterceptors
import org.librarysimplified.http.vanilla.LSHTTPClients

class LSHTTPRefreshTokenTest : LSHTTPRefreshTokenContract() {
  override fun clients(parsers: LSHTTPProblemReportParserFactoryType): LSHTTPClientProviderType {
    return LSHTTPClients(
      parsers,
      listOf(LSHTTPRefreshTokenInterceptors())
    )
  }
}
