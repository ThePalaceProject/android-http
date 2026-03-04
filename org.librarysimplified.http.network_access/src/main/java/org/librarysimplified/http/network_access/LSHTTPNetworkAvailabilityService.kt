package org.librarysimplified.http.network_access

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import org.librarysimplified.http.api.LSHTTPNetworkAccess
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A service that monitors network availability.
 */

class LSHTTPNetworkAvailabilityService : Service() {

  private val logger =
    LoggerFactory.getLogger(LSHTTPNetworkAvailabilityService::class.java)

  companion object {
    private val isRunning =
      AtomicBoolean(false)

    fun isRunning(): Boolean {
      return this.isRunning.get()
    }
  }

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int,
  ): Int {
    if (isRunning.compareAndSet(false, true)) {
      this.logger.debug("Starting network access service…")

      try {
        val cellularRequest =
          NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val wifiRequest =
          NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val cellularCallback =
          object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
              network: Network,
              networkCapabilities: NetworkCapabilities,
            ) {
              super.onCapabilitiesChanged(network, networkCapabilities)
              if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                LSHTTPNetworkAccess.setCellularAvailable(true)
              } else {
                LSHTTPNetworkAccess.setCellularAvailable(false)
              }
            }

            override fun onAvailable(network: Network) {
              super.onAvailable(network)
              this@LSHTTPNetworkAvailabilityService.logger.debug("Cellular became available.")
              LSHTTPNetworkAccess.setCellularAvailable(true)
            }

            override fun onLost(network: Network) {
              super.onLost(network)
              this@LSHTTPNetworkAvailabilityService.logger.debug("Cellular became unavailable (Lost).")
              LSHTTPNetworkAccess.setCellularAvailable(false)
            }

            override fun onUnavailable() {
              super.onUnavailable()
              this@LSHTTPNetworkAvailabilityService.logger.debug("Cellular became unavailable (Unavailable).")
              LSHTTPNetworkAccess.setCellularAvailable(false)
            }
          }

        val wifiCallback =
          object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
              network: Network,
              networkCapabilities: NetworkCapabilities,
            ) {
              super.onCapabilitiesChanged(network, networkCapabilities)
              if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                LSHTTPNetworkAccess.setWIFIAvailable(true)
              } else {
                LSHTTPNetworkAccess.setWIFIAvailable(false)
              }
            }

            override fun onAvailable(network: Network) {
              super.onAvailable(network)
              this@LSHTTPNetworkAvailabilityService.logger.debug("WIFI became available.")
              LSHTTPNetworkAccess.setWIFIAvailable(true)
            }

            override fun onLost(network: Network) {
              super.onLost(network)
              this@LSHTTPNetworkAvailabilityService.logger.debug("WIFI became unavailable (Lost).")
              LSHTTPNetworkAccess.setWIFIAvailable(false)
            }

            override fun onUnavailable() {
              super.onUnavailable()
              this@LSHTTPNetworkAvailabilityService.logger.debug("WIFI became unavailable (Unavailable).")
              LSHTTPNetworkAccess.setWIFIAvailable(false)
            }
          }

        val connectivityManager =
          this.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

        connectivityManager.requestNetwork(wifiRequest, wifiCallback)
        connectivityManager.requestNetwork(cellularRequest, cellularCallback)

        this.determineInitialNetworkState(connectivityManager)
      } catch (e: Throwable) {
        this.logger.debug("Failed to start network service: ", e)
        isRunning.set(false)
      }
    } else {
      this.logger.debug("Ignoring redundant request to start network service.")
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun determineInitialNetworkState(
    connectivityManager: ConnectivityManager,
  ) {
    var cellularAvailable = false
    var wifiAvailable = false

    try {
      val active = connectivityManager.activeNetwork
      if (active != null) {
        val caps = connectivityManager.getNetworkCapabilities(active)
        if (caps != null) {
          if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
          ) {
            wifiAvailable =
              caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            cellularAvailable =
              caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
          }
        }
      }
    } catch (e: Throwable) {
      this.logger.debug("Initial network state: Failed: ", e)
      cellularAvailable = true
      wifiAvailable = true
    }

    if (wifiAvailable) {
      this.logger.debug("Initial network state: WIFI AVAILABLE")
    } else {
      this.logger.debug("Initial network state: WIFI UNAVAILABLE")
    }

    if (cellularAvailable) {
      this.logger.debug("Initial network state: Cellular AVAILABLE")
    } else {
      this.logger.debug("Initial network state: Cellular UNAVAILABLE")
    }

    LSHTTPNetworkAccess.setWIFIAvailable(wifiAvailable)
    LSHTTPNetworkAccess.setCellularAvailable(cellularAvailable)
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}
