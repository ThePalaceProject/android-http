package org.librarysimplified.http.api

import one.irradia.mime.api.MIMEType

/**
 * A mutable request builder.
 */

interface LSHTTPRequestBuilderType {

  /**
   * A specification of whether or not redirects should be allowed.
   */

  enum class AllowRedirects {

    /**
     * Redirects should be followed automatically.
     */

    ALLOW_REDIRECTS,

    /**
     * Redirects should never be followed.
     */

    DISALLOW_REDIRECTS,

    /**
     * Redirects between HTTP and HTTPS are allowed. This is unsafe!
     */

    ALLOW_UNSAFE_REDIRECTS,
  }

  /**
   * Add an HTTP header to the request.
   */

  fun addHeader(
    name: String,
    value: String,
  ): LSHTTPRequestBuilderType

  /**
   * Remove an HTTP header from the request.
   */

  fun removeHeader(
    name: String,
  ): LSHTTPRequestBuilderType

  /**
   * Specify redirect behaviour. The default is [AllowRedirects.ALLOW_REDIRECTS].
   */

  fun allowRedirects(
    redirects: AllowRedirects,
  ): LSHTTPRequestBuilderType

  /**
   * The HTTP method used.
   */

  sealed class Method {
    object Get : Method()
    object Head : Method()
    object Delete : Method()

    data class Post(
      val body: ByteArray,
      val contentType: MIMEType,
    ) : Method()

    data class Put(
      val body: ByteArray,
      val contentType: MIMEType,
    ) : Method()
  }

  /**
   * Set the HTTP method used. The default is [Method.Get].
   */

  fun setMethod(
    method: Method,
  ): LSHTTPRequestBuilderType

  /**
   * Set the HTTP authorization. Note that this typically results in the implicit addition of an
   * `Authorization` header to the resulting HTTP request, but an `Authorization` header set
   * explicitly using [addHeader] will take precedence. If this is a problem, use [removeHeader]
   * before calling [setAuthorization] to ensure that no preexisting `Authorization` is used.
   */

  fun setAuthorization(
    authorization: LSHTTPAuthorizationType?,
  ): LSHTTPRequestBuilderType

  /**
   * Add a cookie to the request.
   */

  fun addCookie(
    name: String,
    value: String,
  ): LSHTTPRequestBuilderType

  /**
   * Remove a cookie from the request.
   */

  fun removeCookie(
    name: String,
  ): LSHTTPRequestBuilderType

  /**
   * Remove all cookies from the request.
   */

  fun removeAllCookies(): LSHTTPRequestBuilderType

  /**
   * Set a function that is evaluated for each actual HTTP request made to the server. This
   * function will be evaluated exactly once for each request made to the server (including
   * each redirect, if any). Note that the modifier function becomes fully responsible for
   * enacting any security policy such as setting and unsetting `Authorization` headers across
   * domains if that is required.
   *
   * Note: Most applications will _not_ need to use this method. This is to be considered advanced
   * functionality that should be used sparingly, if at all. Consider creating an interceptor
   * extension before trying to use this method.
   */

  fun setRequestModifier(
    modifier: (LSHTTPRequestProperties) -> LSHTTPRequestProperties,
  ): LSHTTPRequestBuilderType

  /**
   * Set an extension property of indefinite type.
   */

  fun setExtensionProperty(
    key: String,
    value: Any,
  ): LSHTTPRequestBuilderType

  /**
   * Set a function that is evaluated for each actual HTTP response from the server. This
   * function will be evaluated exactly once for each response returned by the server (including
   * each redirect, if any).
   *
   * Note: Most applications will _not_ need to use this method. This is to be considered advanced
   * functionality that should be used sparingly, if at all. Consider creating an interceptor
   * extension before trying to use this method.
   */

  fun setResponseObserver(
    observer: (LSHTTPResponseType) -> Unit,
  ): LSHTTPRequestBuilderType

  /**
   * Build an immutable request based on the parameters given so far.
   */

  fun build(): LSHTTPRequestType
}
