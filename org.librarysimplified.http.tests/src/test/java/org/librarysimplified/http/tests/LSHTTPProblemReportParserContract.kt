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

    /*
     * A document without the Palace `show_title` extension displays its title, exactly as
     * it did before the extension existed.
     */

    Assertions.assertEquals(null, status.showTitle)
    Assertions.assertTrue(status.shouldShowTitle)
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
    Assertions.assertEquals(null, status.showTitle)
    Assertions.assertTrue(status.shouldShowTitle)
  }

  /**
   * `show_title: false` is parsed, and asks callers to render `detail` on its own.
   */

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

    Assertions.assertEquals(false, status.showTitle)
    Assertions.assertFalse(status.shouldShowTitle)

    /*
     * The title is still parsed and still available; only its display is suppressed.
     */

    Assertions.assertEquals("Blocked by library policy.", status.title)
    Assertions.assertEquals("Please sign in at your local library instead.", status.detail)
  }

  /**
   * `show_title: true` is parsed, and is equivalent to omitting the member.
   */

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

    Assertions.assertEquals(true, status.showTitle)
    Assertions.assertTrue(status.shouldShowTitle)
    Assertions.assertEquals("Blocked by library policy.", status.title)
  }

  /**
   * A `show_title` that is not a boolean does not cost us the rest of the document. The
   * flag degrades to "absent", and everything else parses as usual.
   *
   * This is the case that matters most on the sign-in path: a caller that loses the whole
   * problem report also loses the server's `detail`, and a patron blocked by library policy
   * would be told their credentials were invalid instead of seeing their library's message.
   */

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

    Assertions.assertEquals(null, status.showTitle)
    Assertions.assertTrue(status.shouldShowTitle)

    Assertions.assertEquals("Blocked by library policy.", status.title)
    Assertions.assertEquals("Please sign in at your local library instead.", status.detail)
    Assertions.assertEquals(403, status.status)
  }

  /**
   * The `show_title` attribute appears in `toMap()` only when the server actually sent the
   * member, so reports without the extension keep the exact attribute set that every
   * existing consumer sees today.
   */

  @Test
  fun testToMapShowTitle() {
    val parsers = this.parsers()

    val withoutFlag =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(LSHTTPTestDirectories::class.java, this.testDirectory, "valid0.json"),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertFalse(withoutFlag.toMap().containsKey("HTTP problem show title"))

    val withFlag =
      parsers.createParser(
        "urn:test",
        resourceStreamOf(
          LSHTTPTestDirectories::class.java,
          this.testDirectory,
          "showTitleFalse.json",
        ),
      ).use(LSHTTPProblemReportParserType::execute)

    Assertions.assertEquals("false", withFlag.toMap()["HTTP problem show title"])
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
