package org.librarysimplified.http.vanilla

import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPProblemReportParserType
import org.librarysimplified.http.vanilla.internal.LSHTTPProblemReportParser
import java.io.InputStream

/**
 * The default problem parser implementation.
 */

class LSHTTPProblemReportParsers : LSHTTPProblemReportParserFactoryType {
  override fun createParser(
    uri: String,
    stream: InputStream,
  ): LSHTTPProblemReportParserType {
    return LSHTTPProblemReportParser(uri, stream)
  }
}
