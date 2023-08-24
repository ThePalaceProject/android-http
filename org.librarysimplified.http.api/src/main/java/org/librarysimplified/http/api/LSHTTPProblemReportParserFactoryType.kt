package org.librarysimplified.http.api

import java.io.InputStream

/**
 * A factory of problem report parsers.
 */

interface LSHTTPProblemReportParserFactoryType {

  /**
   * Create a new parser for the given input stream. The provided URI is used for
   * diagnostic messages.
   */

  fun createParser(
    uri: String,
    stream: InputStream,
  ): LSHTTPProblemReportParserType
}
