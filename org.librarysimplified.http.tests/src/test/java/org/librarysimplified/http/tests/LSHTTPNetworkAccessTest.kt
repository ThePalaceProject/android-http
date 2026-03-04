package org.librarysimplified.http.tests

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPNetworkAccess

class LSHTTPNetworkAccessTest {

  @Test
  fun testPermitted0() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertTrue(LSHTTPNetworkAccess.canDownload())
  }

  @Test
  fun testPermitted1() {
    LSHTTPNetworkAccess.setCellularAvailable(false)
    LSHTTPNetworkAccess.setCellularPermitted(false)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertTrue(LSHTTPNetworkAccess.canDownload())
  }

  @Test
  fun testPermitted2() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(false)
    LSHTTPNetworkAccess.setWIFIPermitted(false)

    Assertions.assertTrue(LSHTTPNetworkAccess.canDownload())
  }

  @Test
  fun testAvailableNotPermitted0() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(false)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(false)

    Assertions.assertFalse(LSHTTPNetworkAccess.canDownload())
  }

  @Test
  fun testPermittedNotAvailable0() {
    LSHTTPNetworkAccess.setCellularAvailable(false)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(false)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertFalse(LSHTTPNetworkAccess.canDownload())
  }
}
