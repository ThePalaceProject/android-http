package org.librarysimplified.http.api

import java.io.Serializable

/**
 * An RFC 7807 problem report.
 *
 * @see "https://tools.ietf.org/html/rfc7807"
 *
 * @property showTitle A Palace extension (`show_title`) indicating whether clients should
 *                     display a title alongside [detail]. Servers send `false` when [detail]
 *                     is meant to stand on its own; the member is omitted otherwise, so `null`
 *                     is the common case. Prefer [shouldShowTitle] over reading this directly.
 */

data class LSHTTPProblemReport(
  val status: Int?,
  val title: String?,
  val detail: String?,
  val type: String?,
  val showTitle: Boolean? = null,
) : Serializable {

  /**
   * Whether a title should be displayed alongside [detail].
   *
   * `true` when the server sent no `show_title` member, so documents that predate the
   * extension keep displaying a title exactly as they always have.
   *
   * @see showTitle
   */

  val shouldShowTitle: Boolean
    get() = this.showTitle ?: true

  /**
   * @return The current report as a set of named attributes
   */

  fun toMap(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    attributes["HTTP problem detail"] = this.detail ?: ""
    attributes["HTTP problem status"] = this.status.toString()
    attributes["HTTP problem title"] = this.title ?: ""
    attributes["HTTP problem type"] = this.type.toString()

    /*
     * Only present when the server actually sent the member, so reports without the
     * extension keep the exact attribute set that every existing consumer sees today.
     */

    val showTitleNow = this.showTitle
    if (showTitleNow != null) {
      attributes["HTTP problem show title"] = showTitleNow.toString()
    }
    return attributes.toMap()
  }
}
