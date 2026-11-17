package org.librarysimplified.http.api

import java.io.Serializable

/**
 * An RFC 7807 problem report.
 *
 * @see "https://tools.ietf.org/html/rfc7807"
 *
 * @property showTitle A Palace extension (`show_title`) indicating whether clients should
 *                     display [title] alongside [detail]. Servers send `false` when [detail]
 *                     is meant to stand on its own, and omit the member otherwise. Note that
 *                     this is independent of whether [title] is actually present.
 */

data class LSHTTPProblemReport(
  val status: Int?,
  val title: String?,
  val detail: String?,
  val type: String?,
  val showTitle: Boolean = true,
) : Serializable {

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
