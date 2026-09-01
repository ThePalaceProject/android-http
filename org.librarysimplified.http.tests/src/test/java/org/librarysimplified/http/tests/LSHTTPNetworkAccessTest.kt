package org.librarysimplified.http.tests

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.librarysimplified.http.api.LSHTTPNetworkAccess
import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_AVAILABLE
import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_NOT_PERMITTED
import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_UNAVAILABLE

class LSHTTPNetworkAccessTest {

  @Test
  fun testPermitted0() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertEquals(NETWORK_AVAILABLE, LSHTTPNetworkAccess.canUseNetwork())
  }

  @Test
  fun testPermitted1() {
    LSHTTPNetworkAccess.setCellularAvailable(false)
    LSHTTPNetworkAccess.setCellularPermitted(false)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertEquals(NETWORK_AVAILABLE, LSHTTPNetworkAccess.canUseNetwork())
  }

  @Test
  fun testPermitted2() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(false)
    LSHTTPNetworkAccess.setWIFIPermitted(false)

    Assertions.assertEquals(NETWORK_AVAILABLE, LSHTTPNetworkAccess.canUseNetwork())
  }

  @Test
  fun testAvailableNotPermitted0() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setCellularPermitted(false)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    LSHTTPNetworkAccess.setWIFIPermitted(false)

    Assertions.assertEquals(NETWORK_NOT_PERMITTED, LSHTTPNetworkAccess.canUseNetwork())
  }

  @Test
  fun testPermittedNotAvailable0() {
    LSHTTPNetworkAccess.setCellularAvailable(false)
    LSHTTPNetworkAccess.setCellularPermitted(true)
    LSHTTPNetworkAccess.setWIFIAvailable(false)
    LSHTTPNetworkAccess.setWIFIPermitted(true)

    Assertions.assertEquals(NETWORK_UNAVAILABLE, LSHTTPNetworkAccess.canUseNetwork())
  }

  @Test
  fun testAvailable0() {
    LSHTTPNetworkAccess.setCellularAvailable(true)
    LSHTTPNetworkAccess.setWIFIAvailable(true)
    Assertions.assertTrue(LSHTTPNetworkAccess.anyAvailable.get())

    LSHTTPNetworkAccess.setCellularAvailable(false)
    Assertions.assertTrue(LSHTTPNetworkAccess.anyAvailable.get())

    LSHTTPNetworkAccess.setWIFIAvailable(false)
    Assertions.assertFalse(LSHTTPNetworkAccess.anyAvailable.get())

    LSHTTPNetworkAccess.setCellularAvailable(true)
    Assertions.assertTrue(LSHTTPNetworkAccess.anyAvailable.get())
  }
}
