package org.librarysimplified.http.tests

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPProblemReportParserFactoryType
import org.librarysimplified.http.api.LSHTTPProblemReportParserType
import org.librarysimplified.http.tests.LSHTTPTestDirectories.resourceStreamOf
import java.io.File
import java.io.IOException
import java.util.ServiceLoader

abstract class LSHTTPProblemReportParserContract {

  private lateinit var testDirectory: File

  abstract fun parsers(): LSHTTPProblemReportParserFactoryType

  @BeforeEach
  fun testSetup() {
    this.testDirectory = LSHTTPTestDirectories.createTempDirectory()
  }

  @Test
  fun testValid() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, "valid0.json"),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertEquals("https://example.com/probs/out-of-credit", status.type)
    Assertions.assertEquals("You do not have enough credit.", status.title)
    Assertions.assertEquals("Your current balance is 30, but that costs 50.", status.detail)
    Assertions.assertEquals(200, status.status)
  }

  @Test
  fun testEmpty() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, "valid1.json"),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertEquals(null, status.type)
    Assertions.assertEquals(null, status.title)
    Assertions.assertEquals(null, status.detail)
    Assertions.assertEquals(null, status.status)
  }

  @Test
  fun testUnparseable0() {
    val parsers = this.parsers()

    Assertions.assertThrows(IOException::class.java) {
      parsers.createParser(
        "urn:test",
        resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, "invalid0.json"),
      ).use(LSHTTPProblemReportParserType::execute)
    }
  }

  @Test
  fun testUnparseable1() {
    val parsers = this.parsers()

    Assertions.assertThrows(IOException::class.java) {
      parsers.createParser(
        "urn:test",
        resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, "invalid1.json"),
      ).use(LSHTTPProblemReportParserType::execute)
    }
  }

  @Test
  fun testParserService() {
    val service =
      ServiceLoader.load(LSHTTPProblemReportParserFactoryType::class.java)
        .first()
  }
}
