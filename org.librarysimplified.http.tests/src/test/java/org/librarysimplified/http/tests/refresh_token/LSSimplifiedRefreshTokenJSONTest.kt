package org.librarysimplified.http.tests.refresh_token

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.librarysimplified.http.refresh_token.internal.LSSimplifiedRefreshTokenJSON
import java.io.ByteArrayInputStream

class LSSimplifiedRefreshTokenJSONTest {

  @Test
  fun testDeserializeFromStream() {
    val str = "{\"accessToken\":\"token\"}"
    val refreshToken =
      LSSimplifiedRefreshTokenJSON.deserializeFromStream(ByteArrayInputStream(str.toByteArray()))

    assertEquals(refreshToken.accessToken, "token")
  }

  @Test
  fun testNullInputStream() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedRefreshTokenJSON.deserializeFromStream(null)
    }

    assertTrue(ex.message!!.contains("Invalid input stream"))
  }

  @Test
  fun testInvalidInputStream() {
    val ex = Assertions.assertThrows(Exception::class.java) {
      LSSimplifiedRefreshTokenJSON.deserializeFromStream(ByteArrayInputStream("{}".toByteArray()))
    }

    assertTrue(ex.message!!.contains("Cannot construct instance of"))
  }
}
