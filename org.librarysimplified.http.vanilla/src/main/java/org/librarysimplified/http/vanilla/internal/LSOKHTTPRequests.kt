package org.librarysimplified.http.vanilla.internal

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.librarysimplified.http.api.LSHTTPRequestBuilderType
import org.librarysimplified.http.api.LSHTTPRequestProperties

object LSOKHTTPRequests {

  fun createRequest(
    properties: LSHTTPRequestProperties,
  ): Request {
    val builder = Request.Builder().url(properties.target.toURL())
    return this.createRequestForBuilder(properties, builder)
  }

  fun createRequestForBuilder(
    properties: LSHTTPRequestProperties,
    builder: Request.Builder,
  ): Request {
    val authorization = properties.authorization
    if (authorization != null) {
      builder.header("Authorization", authorization.toHeaderValue())
    } else {
      builder.removeHeader("Authorization")
    }

    if (properties.cookies.isNotEmpty()) {
      val headerText =
        properties.cookies.entries.fold(
          initial = "",
          operation = { acc, entry -> acc + "${entry.key}=${entry.value};" },
        )
      builder.header("Cookie", headerText)
    }

    for ((name, value) in properties.headers) {
      builder.header(name, value)
    }

    when (val method = properties.method) {
      LSHTTPRequestBuilderType.Method.Get -> {
        builder.get()
      }
      LSHTTPRequestBuilderType.Method.Head -> {
        builder.head()
      }
      is LSHTTPRequestBuilderType.Method.Post -> {
        val bytes = method.body
        val type = method.contentType.fullType
        builder.post(bytes.toRequestBody(type.toMediaType()))
      }
      is LSHTTPRequestBuilderType.Method.Put -> {
        val bytes = method.body
        val type = method.contentType.fullType
        builder.put(bytes.toRequestBody(type.toMediaType()))
      }
      LSHTTPRequestBuilderType.Method.Delete -> {
        builder.delete()
      }
    }

    builder.tag(LSHTTPRequestProperties::class.java, properties)
    return builder.build()
  }
}
