package org.polyfrost.crashpatch.client.crashes

import dev.deftu.omnicore.api.gameDirectory
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.crashes.cache.CacheResult
import org.polyfrost.crashpatch.client.crashes.cache.CrashDataCache
import org.polyfrost.crashpatch.client.crashes.data.CrashData
import org.polyfrost.crashpatch.client.crashes.data.FixType
import org.polyfrost.oneconfig.utils.v1.Multithreading
import java.io.File
import kotlin.collections.set

object CrashScanner {
    private const val DISCONNECT = "Disconnect reason"
    private const val CRASH = "Crash log"

    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Crash Scan Storage")

    private val comparator = Comparator<String> { o1, o2 ->
        when {
            o1 == CRASH || o1 == DISCONNECT -> 1
            o2 == CRASH || o2 == DISCONNECT -> -1
            else -> o1.compareTo(o2)
        }
    }

    private val placeholders = mutableListOf<ScanPlaceholder<*>>()
    private var data: CrashData? = null

    @JvmStatic
    fun initialize() {
        registerDefaultPlaceholders()
        submitCacheRequest()
    }

    @JvmStatic
    fun submitCacheRequest() {
        Multithreading.submit {
            when (val result = CrashDataCache.cache()) {
                is CacheResult.Success -> {
                    data = result.data
                    LOGGER.info("Successfully cached crash data JSON with ${data?.fixes?.size ?: 0} fixes and ${data?.fixTypes?.size ?: 0} fix types!")
                }

                is CacheResult.Failure -> {
                    LOGGER.error("Failed to cache crash data JSON!", result.reason)
                }
            }
        }
    }

    @JvmStatic
    fun scan(text: String, isServerCrash: Boolean = false): CrashScan? {
        return try {
            val data = data ?: return null
            val responses = compileResponses(data, text, isServerCrash)
            val lines = text.split("\\R".toRegex())
            val key = if (isServerCrash) "Disconnect reason" else "Crash log"

            val augment = responses.toMutableMap()
            augment[key] = lines

            CrashScan(augment.toSortedMap(comparator).map { map ->
                CrashScan.Solution("${map.key} (${map.value.size})", map.value.map { entry ->
                    var replaced = entry
                    placeholders.forEach { placeholder ->
                        replaced = placeholder.replace(replaced)
                    }

                    replaced
                }, true)
            }.toMutableList())
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    private fun compileResponses(catalog: CrashData, report: String, isServerCrash: Boolean): MutableMap<String, List<String>> {
        val responses: LinkedHashMap<String, ArrayList<String>> = linkedMapOf()
        val ignoredTypeIds: HashSet<Int> = hashSetOf()

        catalog.fixTypes.forEachIndexed { idx: Int, type: FixType ->
            val ignore: Boolean =
                type.noInGameDisplay ||
                (type.serverCrashes && !isServerCrash) ||
                (!type.serverCrashes && isServerCrash)

            if (ignore) {
                ignoredTypeIds += idx
            }

            responses[type.name] = arrayListOf()
        }

        if (catalog.fixTypes.isEmpty()) {
            return linkedMapOf()
        }

        val typeNames: List<String> = catalog.fixTypes.map { it.name }
        for (fix in catalog.fixes) {
            val typeId: Int = catalog.resolveId(fix) ?: continue
            if (typeId in ignoredTypeIds) {
                continue
            }

            if (!fix.triggersOn(report)) {
                continue
            }

            val bucketName: String = typeNames.getOrNull(typeId) ?: continue
            responses[bucketName]?.add(fix.fixText)
        }

        return responses
            .filterValues { it.isNotEmpty() }
            .mapValues { it.value.toList() }
            .toMutableMap()
    }

    private fun registerDefaultPlaceholders() {
        placeholders.add(ScanPlaceholder("%pathindicator%") { "" })
        placeholders.add(ScanPlaceholder("%gameroot%") { gameDirectory.toAbsolutePath().toString().removeSuffix(File.separator) })
        placeholders.add(ScanPlaceholder("%profileroot%") { gameDirectory.resolve("OneConfig").parent.toAbsolutePath().toString().removeSuffix(File.separator) })
    }
}
