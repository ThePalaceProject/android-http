package org.librarysimplified.http.uri_builder

import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
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

  /**
   * Parse the query parameters from a URI and return them as a list of
   * name/value pairs.
   *
   * If the URI has no query component, an empty list is returned.
   *
   * @param uri The URI whose query string to parse
   *
   * @return A list of decoded name/value pairs
   */

  fun decodeQuery(uri: URI): List<Pair<String, String>> {
    val query = uri.query
    if (query == null || query.isEmpty()) {
      return emptyList()
    }

    val pairs = mutableListOf<Pair<String, String>>()
    val segments = query.split("&")
    for (segment in segments) {
      if (segment.isEmpty()) {
        continue
      }
      val eqIndex = segment.indexOf('=')
      if (eqIndex >= 0) {
        val name = URLDecoder.decode(segment.substring(0, eqIndex), "UTF-8")
        val value = URLDecoder.decode(segment.substring(eqIndex + 1), "UTF-8")
        pairs.add(Pair(name, value))
      } else {
        val name = URLDecoder.decode(segment, "UTF-8")
        pairs.add(Pair(name, ""))
      }
    }
    return pairs
  }
}
