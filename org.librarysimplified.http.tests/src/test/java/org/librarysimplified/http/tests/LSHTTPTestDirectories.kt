package org.librarysimplified.http.tests

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID

object LSHTTPTestDirectories {

  private val LOGGER: Logger =
    LoggerFactory.getLogger(LSHTTPTestDirectories::class.java)

  @Throws(IOException::class)
  fun createBaseDirectory(): File {
    val path = File(System.getProperty("java.io.tmpdir"), "lshttp")
    path.mkdirs()
    return path
  }

  @Throws(IOException::class)
  fun createTempDirectory(): File {
    val path = createBaseDirectory()
    val temp = File(path, UUID.randomUUID().toString())
    LOGGER.debug("mkdirs: {}", temp)
    temp.mkdirs()
    return temp
  }

  @Throws(IOException::class)
  fun resourceOf(
    clazz: Class<*> = LSHTTPTestDirectories::class.java,
    output: File,
    name: String,
  ): File {
    val internal = String.format("/org/librarysimplified/http/tests/%s", name)
    val url = clazz.getResource(internal) ?: throw NoSuchFileException(File(internal))
    val target = File(output, name)
    LOGGER.debug("copy {} {}", name, target)
    url.openStream().use { stream -> stream.copyTo(target.outputStream()) }
    return target
  }

  @Throws(IOException::class)
  fun resourceStreamOf(
    clazz: Class<*> = LSHTTPTestDirectories::class.java,
    output: File,
    name: String,
  ): InputStream {
    return resourceOf(clazz, output, name).inputStream()
  }

  fun stringOf(
    clazz: Class<*> = LSHTTPTestDirectories::class.java,
    output: File,
    name: String,
  ): String {
    return String(resourceOf(clazz, output, name).inputStream().readBytes())
  }
}
