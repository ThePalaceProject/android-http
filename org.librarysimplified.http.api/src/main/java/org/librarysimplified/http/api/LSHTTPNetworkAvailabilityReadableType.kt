package org.librarysimplified.http.api

import com.io7m.jattribute.core.AttributeReadableType

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkAvailabilityReadableType {

  val wifiAvailable: AttributeReadableType<Boolean>

  val cellularAvailable: AttributeReadableType<Boolean>
}
