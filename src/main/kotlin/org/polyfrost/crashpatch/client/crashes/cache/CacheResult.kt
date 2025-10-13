package org.polyfrost.crashpatch.client.crashes.cache

import org.polyfrost.crashpatch.client.crashes.data.CrashData

sealed interface CacheResult {
    data class Success(val data: CrashData) : CacheResult
    data class Failure(val reason: Exception) : CacheResult
}
