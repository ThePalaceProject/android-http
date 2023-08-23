package org.librarysimplified.http.oauth_client_credentials.internal

import org.joda.time.LocalDateTime

class LSHTTPOAuthTokenRepository<K, V> {

  data class Expirable<V>(
    val item: V,
    val expiresAt: LocalDateTime,
  )

  private val locks: MutableMap<K, Any> =
    mutableMapOf()

  private val items: MutableMap<K, Expirable<V>> =
    mutableMapOf()

  fun getOrRefresh(key: K, refresh: (K) -> Expirable<V>): V {
    // Get or create a per-key lock while holding a global lock
    val lock = synchronized(locks) {
      locks[key] ?: Any().also { locks[key] = it }
    }

    // Get or refresh the item while holding the per-key lock.
    return synchronized(lock) {
      val item = items[key]
        ?.takeUnless { it.expiresAt < LocalDateTime.now() }
        ?: refresh(key).also { items[key] = it }
      item.item
    }
  }
}
