package org.librarysimplified.http.api

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkAvailabilityType : LSHTTPNetworkAvailabilityReadableType {

  fun setWIFIAvailable(
    available: Boolean,
  )

  fun setCellularAvailable(
    available: Boolean,
  )
}
