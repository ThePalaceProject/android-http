package org.librarysimplified.http.api

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkAccessReadableType :
  LSHTTPNetworkAvailabilityReadableType,
  LSHTTPNetworkPolicyReadableType {
  /**
   * Determine if performing a download should be allowed given the current network availability
   * and permissions.
   */

  fun canDownload(): Boolean {
    if (this.wifiAvailable.get() && this.wifiPermitted.get()) {
      return true
    }
    if (this.cellularAvailable.get() && this.cellularPermitted.get()) {
      return true
    }
    return false
  }
}
