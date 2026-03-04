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
            override fun onAvailable(network: Network) {
              super.onAvailable(network)
              LSHTTPNetworkAccess.setCellularAvailable(true)
            }

            override fun onLost(network: Network) {
              super.onLost(network)
              LSHTTPNetworkAccess.setCellularAvailable(false)
            }

            override fun onUnavailable() {
              super.onUnavailable()
              LSHTTPNetworkAccess.setCellularAvailable(false)
            }
          }

        val wifiCallback =
          object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
              super.onAvailable(network)
              LSHTTPNetworkAccess.setWIFIAvailable(true)
            }

            override fun onLost(network: Network) {
              super.onLost(network)
              LSHTTPNetworkAccess.setWIFIAvailable(false)
            }

            override fun onUnavailable() {
              super.onUnavailable()
              LSHTTPNetworkAccess.setWIFIAvailable(false)
            }
          }

        val connectivityManager =
          this.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

        connectivityManager.requestNetwork(wifiRequest, wifiCallback)
        connectivityManager.requestNetwork(cellularRequest, cellularCallback)
      } catch (e: Throwable) {
        this.logger.debug("Failed to start network service: ", e)
        isRunning.set(false)
      }
    } else {
      this.logger.debug("Ignoring redundant request to start network service.")
    }
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}
