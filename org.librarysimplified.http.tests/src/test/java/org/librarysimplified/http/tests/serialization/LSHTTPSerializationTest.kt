package org.librarysimplified.http.tests.serialization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPProblemReport
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LSHTTPSerializationTest {

  @Test
  fun testProblemReportSerialization() {
    val problemReport = LSHTTPProblemReport(
      status = 1,
      title = "Dummy Title",
      detail = "Dummy detail",
      type = "Common error"
    )
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(problemReport)
    val byteArray = byteArrayOutputStream.toByteArray()

    val byteArrayInputStream = ByteArrayInputStream(byteArray)
    val objectInputStream = ObjectInputStream(byteArrayInputStream)

    val deserializedProblemReport = objectInputStream.readObject()

    Assertions.assertEquals(problemReport, deserializedProblemReport)
  }

}
