package org.librarysimplified.http.api

import java.io.Closeable
import java.io.IOException

/**
 * A problem report parser. A parser should be executed once and then closed.
 */

interface LSHTTPProblemReportParserType : Closeable {
  /**
   * Execute the parser, raising an exception in the case of parse errors.
   */

  @Throws(IOException::class)
  fun execute(): LSHTTPProblemReport
}
