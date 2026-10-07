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
   * This says only what the server asked for, and is independent of whether [title] is
   * actually present: a report with no title at all still reports `true` here. Callers
   * must check [title] for null regardless of this value.
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
     * Note that [showTitle] is deliberately _not_ included here. These attributes are
     * diagnostic: they end up on the task recorder, and from there are rendered verbatim
     * onto the error page that patrons can open. `show_title` is a rendering directive
     * rather than a statement about what went wrong, and the documents that carry it are
     * exactly the ones where a library has asked for less framing around its message, so
     * surfacing it to the patron would work against the reason it exists.
     */

    return attributes.toMap()
  }

  companion object {

    /*
     * Pinned as of the addition of [showTitle]. Without an explicit value the JVM derives
     * one from the class structure, so every future field addition would silently break
     * deserialization of anything already written. Nothing persists these reports across
     * versions today; this keeps it that way by choice rather than by luck.
     */

    private const val serialVersionUID: Long = 1L
  }
}
