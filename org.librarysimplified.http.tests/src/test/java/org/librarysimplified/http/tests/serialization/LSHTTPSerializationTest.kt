package org.librarysimplified.http.tests.serialization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPProblemReport
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass

class LSHTTPSerializationTest {

  @Test
  fun testProblemReportSerialization() {
    val problemReport = LSHTTPProblemReport(
      status = 1,
      title = "Dummy Title",
      detail = "Dummy detail",
      type = "Common error"
    )

    Assertions.assertEquals(problemReport, roundTrip(problemReport))
  }

  /**
   * The Palace `show_title` extension survives a round trip.
   */

  @Test
  fun testProblemReportSerializationShowTitle() {
    val problemReport = LSHTTPProblemReport(
      status = 403,
      title = "Blocked by library policy.",
      detail = "Please sign in at your local library instead.",
      type = "http://librarysimplified.org/terms/problem/credentials-blocked-by-policy",
      showTitle = false
    )

    val deserialized = roundTrip(problemReport) as LSHTTPProblemReport
    Assertions.assertEquals(problemReport, deserialized)
    Assertions.assertEquals(false, deserialized.showTitle)
    Assertions.assertFalse(deserialized.shouldShowTitle)
  }

  /**
   * The serial version UID is pinned to an explicit value.
   *
   * Without one the JVM derives a UID from the class structure, and every subsequent field
   * added to the report silently invalidates anything already written. This asserts against
   * [ObjectStreamClass] rather than the constant, because the risk being guarded against is
   * precisely that the declaration stops producing a static field the serialization
   * machinery can see.
   */

  @Test
  fun testProblemReportSerialVersionUID() {
    Assertions.assertEquals(
      1L,
      ObjectStreamClass.lookup(LSHTTPProblemReport::class.java).serialVersionUID
    )
  }

  private fun roundTrip(value: Any): Any {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(value) }

    return ObjectInputStream(
      ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    ).use { it.readObject() }
  }
}
