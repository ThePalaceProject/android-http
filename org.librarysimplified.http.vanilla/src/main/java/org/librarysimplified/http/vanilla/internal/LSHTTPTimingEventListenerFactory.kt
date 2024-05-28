package org.librarysimplified.http.vanilla.internal

import okhttp3.Call
import okhttp3.EventListener
import java.util.concurrent.atomic.AtomicLong

object LSHTTPTimingEventListenerFactory : EventListener.Factory {

  private val callId = AtomicLong()

  override fun create(
    call: Call,
  ): EventListener {
    return LSHTTPTimingEventListener(
      id = callId.incrementAndGet(),
    )
  }
}
