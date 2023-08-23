package org.librarysimplified.http.tests.uri_builder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.librarysimplified.http.uri_builder.LSHTTPURIQueryBuilder
import java.net.URI

class LSHTTPURIQueryBuilderTest {

  @Test
  fun testEmpty() {
    val uri =
      LSHTTPURIQueryBuilder.encodeQuery(
        base = URI.create("http://www.example.com"),
        parameters = sortedMapOf(),
      )

    Assertions.assertEquals("http://www.example.com", uri.toString())
  }

  @Test
  fun testABC() {
    val uri =
      LSHTTPURIQueryBuilder.encodeQuery(
        base = URI.create("http://www.example.com"),
        parameters = sortedMapOf(
          Pair("a", "x"),
          Pair("b", "y"),
          Pair("c", "z"),
        ),
      )

    Assertions.assertEquals("http://www.example.com?a=x&b=y&c=z", uri.toString())
  }
}
