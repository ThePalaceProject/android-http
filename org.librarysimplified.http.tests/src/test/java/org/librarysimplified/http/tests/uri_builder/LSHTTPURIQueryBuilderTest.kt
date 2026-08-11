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

  @Test
  fun testDecodeQueryEmpty() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(URI.create("http://www.example.com"))

    Assertions.assertTrue(params.isEmpty())
  }

  @Test
  fun testDecodeQueryNoQuery() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com/path#fragment"),
      )

    Assertions.assertTrue(params.isEmpty())
  }

  @Test
  fun testDecodeQuerySimple() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com?a=x&b=y&c=z"),
      )

    Assertions.assertEquals(3, params.size)
    Assertions.assertEquals(Pair("a", "x"), params[0])
    Assertions.assertEquals(Pair("b", "y"), params[1])
    Assertions.assertEquals(Pair("c", "z"), params[2])
  }

  @Test
  fun testDecodeQueryEncoded() {
    val uri = URI("http", "www.example.com", "/", "name=hello%20world", null)
    val params = LSHTTPURIQueryBuilder.decodeQuery(uri)

    Assertions.assertEquals(1, params.size)
    Assertions.assertEquals(Pair("name", "hello world"), params[0])
  }

  @Test
  fun testDecodeQuerySingleParam() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com?key=value"),
      )

    Assertions.assertEquals(1, params.size)
    Assertions.assertEquals(Pair("key", "value"), params[0])
  }

  @Test
  fun testDecodeQueryNoValue() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com?flag"),
      )

    Assertions.assertEquals(1, params.size)
    Assertions.assertEquals(Pair("flag", ""), params[0])
  }

  @Test
  fun testDecodeQueryEmptyValue() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com?key="),
      )

    Assertions.assertEquals(1, params.size)
    Assertions.assertEquals(Pair("key", ""), params[0])
  }

  @Test
  fun testDecodeQueryDuplicateKeys() {
    val params =
      LSHTTPURIQueryBuilder.decodeQuery(
        URI.create("http://www.example.com?a=1&a=2&b=3"),
      )

    Assertions.assertEquals(3, params.size)
    Assertions.assertEquals(Pair("a", "1"), params[0])
    Assertions.assertEquals(Pair("a", "2"), params[1])
    Assertions.assertEquals(Pair("b", "3"), params[2])
  }

  @Test
  fun testDecodeQueryRoundTrip() {
    val base = URI.create("http://www.example.com")
    val inputParams = sortedMapOf(Pair("name", "test"), Pair("count", "42"))
    val encoded = LSHTTPURIQueryBuilder.encodeQuery(base, inputParams)
    val decoded = LSHTTPURIQueryBuilder.decodeQuery(encoded)

    Assertions.assertEquals(2, decoded.size)
    Assertions.assertEquals(Pair("count", "42"), decoded[0])
    Assertions.assertEquals(Pair("name", "test"), decoded[1])
  }
}
