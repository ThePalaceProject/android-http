package org.librarysimplified.http.api

import com.io7m.jattribute.core.AttributeReadableType
import com.io7m.jattribute.core.Attributes
import org.slf4j.LoggerFactory

/**
 * The main network access service.
 */

object LSHTTPNetworkAccess : LSHTTPNetworkAccessType {

  private val logger =
    LoggerFactory.getLogger(LSHTTPNetworkAccess::class.java)

  private val attributes =
    Attributes.create { x ->
      this.logger.debug("Attribute handler raised exception: ", x)
    }

  private val wifiAvailableProp =
    this.attributes.withValue(false)

  private val cellularAvailableProp =
    this.attributes.withValue(false)

  private val wifiPermittedProp =
    this.attributes.withValue(true)

  private val cellularPermittedProp =
    this.attributes.withValue(true)

  override val wifiAvailable: AttributeReadableType<Boolean>
    get() = this.wifiAvailableProp

  override val cellularAvailable: AttributeReadableType<Boolean>
    get() = this.cellularAvailableProp

  override val wifiPermitted: AttributeReadableType<Boolean>
    get() = this.wifiPermittedProp

  override val cellularPermitted: AttributeReadableType<Boolean>
    get() = this.cellularPermittedProp

  override fun setWIFIPermitted(
    permitted: Boolean,
  ) {
    this.wifiPermittedProp.set(permitted)
  }

  override fun setCellularPermitted(
    permitted: Boolean,
  ) {
    this.cellularPermittedProp.set(permitted)
  }

  override fun setWIFIAvailable(
    available: Boolean,
  ) {
    this.wifiAvailableProp.set(available)
  }

  override fun setCellularAvailable(
    available: Boolean,
  ) {
    this.cellularAvailableProp.set(available)
  }
}
