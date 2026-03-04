package org.librarysimplified.http.api

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkAccessType :
  LSHTTPNetworkAccessReadableType,
  LSHTTPNetworkPolicyType,
  LSHTTPNetworkAvailabilityType
