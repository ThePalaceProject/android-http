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

    Assertions.assertTrue(status.showTitle)
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
    Assertions.assertTrue(status.showTitle)
  }

  @Test
  fun testShowTitleFalse() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(
          LSHTTPTestDirectories::class.java,
          this.testDirectory,
          "showTitleFalse.json",
        ),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertFalse(status.showTitle)

    Assertions.assertEquals("Blocked by library policy.", status.title)
    Assertions.assertEquals("Please sign in at your local library instead.", status.detail)
  }

  @Test
  fun testShowTitleTrue() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(
          LSHTTPTestDirectories::class.java,
          this.testDirectory,
          "showTitleTrue.json",
        ),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertTrue(status.showTitle)
    Assertions.assertEquals("Blocked by library policy.", status.title)
  }

  @Test
  fun testShowTitleNotABoolean() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(
          LSHTTPTestDirectories::class.java,
          this.testDirectory,
          "showTitleInvalid.json",
        ),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertTrue(status.showTitle)

    Assertions.assertEquals("Blocked by library policy.", status.title)
    Assertions.assertEquals("Please sign in at your local library instead.", status.detail)
    Assertions.assertEquals(403, status.status)
  }

  @Test
  fun testShowTitleNull() {
    val parsers = this.parsers()
    val status =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(
          LSHTTPTestDirectories::class.java,
          this.testDirectory,
          "showTitleNull.json",
        ),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertTrue(status.showTitle)

    Assertions.assertEquals("Blocked by library policy.", status.title)
    Assertions.assertEquals("Please sign in at your local library instead.", status.detail)
  }

  @Test
  fun testToMapOmitsShowTitle() {
    val parsers = this.parsers()

    for (file in listOf("valid0.json", "showTitleFalse.json", "showTitleTrue.json")) {
      val status =
        parsers.createParser(
          "urn:test",
          resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, file),
        ).use(LSHTTPProblemReportParserType::execute)

      Assertions.assertEquals(
        setOf(
          "HTTP problem detail",
          "HTTP problem status",
          "HTTP problem title",
          "HTTP problem type",
        ),
        status.toMap().keys,
        file,
      )
    }
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
