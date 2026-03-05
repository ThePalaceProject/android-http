package org.librarysimplified.http.api

import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_AVAILABLE
import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_NOT_PERMITTED
import org.librarysimplified.http.api.LSHTTPNetworkAccessReadableType.LSHTTPNetworkAvailability.NETWORK_UNAVAILABLE

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkAccessReadableType :
  LSHTTPNetworkAvailabilityReadableType,
  LSHTTPNetworkPolicyReadableType {

  /**
   * The reason that downloads may or may not be available.
   */

  enum class LSHTTPNetworkAvailability {
    NETWORK_UNAVAILABLE,
    NETWORK_AVAILABLE,
    NETWORK_NOT_PERMITTED,
  }

  /**
   * Determine if performing network operations should be allowed given the current network
   * availability and permissions.
   */

  fun canUseNetwork(): LSHTTPNetworkAvailability {
    val wifiAvailable = this.wifiAvailable.get()
    val cellAvailable = this.cellularAvailable.get()
    val wifiPermitted = this.wifiPermitted.get()
    val cellPermitted = this.cellularPermitted.get()

    if (!wifiAvailable && !cellAvailable) {
      return NETWORK_UNAVAILABLE
    }

    if (wifiAvailable && wifiPermitted) {
      return NETWORK_AVAILABLE
    }

    if (cellAvailable && cellPermitted) {
      return NETWORK_AVAILABLE
    }

    return NETWORK_NOT_PERMITTED
  }
}
