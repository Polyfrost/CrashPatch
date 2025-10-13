package org.polyfrost.crashpatch.client.crashes.cache

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import dev.deftu.omnicore.api.gameDirectory
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.crashes.data.CrashData
import org.polyfrost.oneconfig.utils.v1.JsonUtils
import kotlin.jvm.optionals.getOrNull

object CrashDataCache {
    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Crash Data Cache")

    private val cacheFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        gameDirectory.resolve("OneConfig/CrashPatch/cache.json").toFile()
    }

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @JvmStatic
    fun cache(): CacheResult {
        return try {
            val jsonData = JsonUtils.parseFromUrl("https://raw.githubusercontent.com/Polyfrost/CrashData/main/crashes.json")
                ?.asJsonObject
                ?: return CacheResult.Failure(IllegalStateException("Failed to download crash data JSON!"))
            if (!cacheFile.exists() && !cacheFile.parentFile.exists()) {
                // Then fail if we can't create the necessary directories and file
                if (!cacheFile.parentFile.mkdirs() && !cacheFile.parentFile.exists()) {
                    return CacheResult.Failure(IllegalStateException("Failed to create cache directory!"))
                }
            }

            cacheFile.writeText(gson.toJson(jsonData))

            val data = parse(jsonData)
                ?: return CacheResult.Failure(IllegalStateException("Could not parse crash data JSON!"))
            CacheResult.Success(data)
        } catch (e: Exception) {
            LOGGER.error("Failed to download crash data JSON!", e)
            cacheFile.takeIf { it.exists() }?.let {
                LOGGER.info("Attempting to load cached crash data JSON...")
                val jsonData = JsonUtils.parseOrNull(it.readText())?.asJsonObject
                    ?: return CacheResult.Failure(e)

                val data = parse(jsonData)
                    ?: return CacheResult.Failure(IllegalStateException("Could not parse cached crash data JSON!"))

                CacheResult.Success(data)
            } ?: CacheResult.Failure(e)
        }
    }

    private fun parse(json: JsonElement): CrashData? {
        return try {
            val data = CrashData.CODEC.parse(JsonOps.INSTANCE, json)
                .result()
                .getOrNull()
            if (data == null) {
                LOGGER.error("Failed to decode crash data JSON!")
                return null
            }

            data
        } catch (e: Exception) {
            LOGGER.error("Failed to decode crash data JSON!", e)
            null
        }
    }
}
