package org.librarysimplified.http.uri_builder

import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder
import java.util.SortedMap

/**
 * Functions for building URI query strings.
 */

object LSHTTPURIQueryBuilder {

  /**
   * Encode a query using the given base URI and set of parameters.
   *
   * @param base       The base URI
   * @param parameters The parameters
   *
   * @return An encoded query
   */

  fun encodeQuery(
    base: URI,
    parameters: SortedMap<String, String>,
  ): URI {
    return try {
      if (parameters.isEmpty()) {
        return base
      }

      val iterator = parameters.keys.iterator()
      val query = StringBuilder(128)
      while (iterator.hasNext()) {
        val name: String = iterator.next()
        val value: String = parameters.get(name)!!
        query.append(URLEncoder.encode(name, "UTF-8"))
        query.append("=")
        query.append(URLEncoder.encode(value, "UTF-8"))
        if (iterator.hasNext()) {
          query.append("&")
        }
      }

      val queryString = query.toString()
      val uriText = base.toASCIIString()
      URI.create("$uriText?$queryString")
    } catch (e: UnsupportedEncodingException) {
      throw IllegalStateException(e)
    }
  }
}
