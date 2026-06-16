package org.librarysimplified.http.vanilla.internal

import one.irradia.mime.api.MIMEType

/**
 * Well-known MIME types.
 */

object LSHTTPMimeTypes {
  val octetStream =
    MIMEType("application", "octet-stream", mapOf())

  val textPlain =
    MIMEType("text", "plain", mapOf())

  val problemReport =
    MIMEType("application", "api-problem+json", mapOf())
}
