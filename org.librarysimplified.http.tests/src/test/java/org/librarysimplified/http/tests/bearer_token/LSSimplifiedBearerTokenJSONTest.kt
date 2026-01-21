package org.librarysimplified.http.tests.bearer_token

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.librarysimplified.http.bearer_token.LSSimplifiedBearerTokenJSON
import java.math.BigInteger
import java.net.URI

class LSSimplifiedBearerTokenJSONTest {

  @Test
  fun testRequiredField0() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedBearerTokenJSON.deserializeFromText("""{}""".trimIndent())
    }

    assertTrue(ex.message!!.contains("access_token"))
  }

  @Test
  fun testRequiredField1() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedBearerTokenJSON.deserializeFromText(
        """{
        "access_token": "abcd"
      }
        """.trimIndent(),
      )
    }

    assertTrue(ex.message!!.contains("expires_in"))
  }

  @Test
  fun testRequiredField2() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedBearerTokenJSON.deserializeFromText(
        """{
        "access_token": "abcd",
        "expires_in": 1000
      }
        """.trimIndent(),
      )
    }

    assertTrue(ex.message!!.contains("location"))
  }

  @Test
  fun badURI0() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedBearerTokenJSON.deserializeFromText(
        """{
        "access_token": "abcd",
        "expires_in": 1000,
        "location": " not a uri "
      }
        """.trimIndent(),
      )
    }

    assertTrue(ex.message!!.contains("location"))
  }

  @Test
  fun ok0() {
    val token =
      LSSimplifiedBearerTokenJSON.deserializeFromText(
        """{
        "access_token": "abcd",
        "expires_in": 1000,
        "location": "https://www.example.com"
      }
        """.trimIndent(),
      )

    assertEquals("abcd", token.accessToken)
    assertEquals(BigInteger.valueOf(1000L), token.expiresIn)
    assertEquals(URI.create("https://www.example.com"), token.location)
  }

  @Test
  fun roundTrip0() {
    val token0 =
      LSSimplifiedBearerTokenJSON.deserializeFromText(
        """{
        "access_token": "abcd",
        "expires_in": 1000,
        "location": "https://www.example.com"
      }
        """.trimIndent(),
      )

    val token1 =
      LSSimplifiedBearerTokenJSON.deserializeFromText(LSSimplifiedBearerTokenJSON.serializeToText(token0))

    assertEquals(token0, token1)
  }
}
