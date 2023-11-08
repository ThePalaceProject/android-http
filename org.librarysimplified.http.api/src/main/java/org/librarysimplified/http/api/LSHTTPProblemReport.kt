package org.librarysimplified.http.api

import java.io.Serializable

/**
 * An RFC 7807 problem report.
 *
 * @see "https://tools.ietf.org/html/rfc7807"
 */

data class LSHTTPProblemReport(
  val status: Int?,
  val title: String?,
  val detail: String?,
  val type: String?,
): Serializable {

  /**
   * @return The current report as a set of named attributes
   */

  fun toMap(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    attributes["HTTP problem detail"] = this.detail ?: ""
    attributes["HTTP problem status"] = this.status.toString()
    attributes["HTTP problem title"] = this.title ?: ""
    attributes["HTTP problem type"] = this.type.toString()
    return attributes.toMap()
  }
}
