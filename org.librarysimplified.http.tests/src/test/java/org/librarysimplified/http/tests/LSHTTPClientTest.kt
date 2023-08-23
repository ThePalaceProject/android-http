package org.librarysimplified.http.tests

import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.vanilla.LSHTTPClients

class LSHTTPClientTest : LSHTTPClientContract() {
  override fun clients(parsers: LSHTTPProblemReportParserFactoryType): LSHTTPClientProviderType {
    return LSHTTPClients(
      parsers,
      listOf(),
    )
  }
}
