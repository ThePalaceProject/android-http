package org.librarysimplified.http.api

import com.io7m.jattribute.core.AttributeReadableType

/**
 * An observable interface that indicates the availability of different network types.
 */

interface LSHTTPNetworkPolicyReadableType {

  val wifiPermitted: AttributeReadableType<Boolean>

  val cellularPermitted: AttributeReadableType<Boolean>
}
