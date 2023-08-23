package org.librarysimplified.http.tests.downloads

import org.librarysimplified.http.api.LSHTTPClientProviderType
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.vanilla.LSHTTPClients

class LSHTTPDownloadsTest : LSHTTPDownloadsContract() {
  override fun clients(parsers: LSHTTPProblemReportParserFactoryType): LSHTTPClientProviderType {
    return LSHTTPClients(
      parsers,
      listOf(),
    )
  }
}
