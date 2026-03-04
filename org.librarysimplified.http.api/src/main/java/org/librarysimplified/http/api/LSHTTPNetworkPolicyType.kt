package org.librarysimplified.http.api

/**
 * An interface that indicates whether it is permitted to use different network types.
 */

interface LSHTTPNetworkPolicyType : LSHTTPNetworkPolicyReadableType {

  fun setWIFIPermitted(
    permitted: Boolean,
  )

  fun setCellularPermitted(
    permitted: Boolean,
  )
}
