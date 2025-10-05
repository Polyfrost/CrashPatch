package org.polyfrost.crashpatch.crashes

import com.google.gson.JsonObject
import net.minecraft.util.EnumChatFormatting
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.client.CrashPatchClient
import org.polyfrost.oneconfig.utils.v1.JsonUtils
import java.io.File
import kotlin.collections.set

object CrashScanStorage {

    private val logger = LogManager.getLogger()

    private val cacheFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        File(CrashPatchClient.mcDir, "OneConfig/CrashPatch/cache.json")
    }

    private val String.mappedPlaceholders: String
        get() = this
            .replace("%pathindicator%", "")
            .replace("%gameroot%", CrashPatchClient.gameDir.absolutePath.removeSuffix(File.separator))
            .replace("%profileroot%", File(CrashPatchClient.mcDir, "OneConfig").parentFile.absolutePath.removeSuffix(File.separator))

    private var skyclientData: JsonObject? = null

    @JvmStatic
    fun downloadJson(): Boolean {
        return try {
            skyclientData = JsonUtils.parseFromUrl("https://raw.githubusercontent.com/Polyfrost/CrashData/main/crashes.json")
                ?.asJsonObject ?: return false
            cacheFile.writeText(skyclientData.toString())
            true
        } catch (e: Exception) {
            logger.error("Failed to download crash data JSON!", e)
            cacheFile.takeIf { it.exists() }?.let {
                logger.info("Attempting to load cached crash data JSON...")
                skyclientData = JsonUtils.parseOrNull(it.readText())?.asJsonObject

                skyclientData != null
            } ?: false
        }
    }

    @JvmStatic
    fun scanReport(report: String, serverCrash: Boolean = false): CrashScan? {
        return try {
            val responses = getResponses(report, serverCrash)
            CrashScan(responses.also { it[if (serverCrash) "Disconnect reason" else "Crash log"] = report.split("\\R".toRegex()) }.toSortedMap { o1, o2 ->
                if (o1 == "Crash log" || o1 == "Disconnect reason") {
                    return@toSortedMap 1
                }

                if (o2 == "Crash log" || o2 == "Disconnect reason") {
                    return@toSortedMap -1
                }

                return@toSortedMap o1.compareTo(o2)
            }.map { map ->
                CrashScan.Solution("${map.key} (${map.value.size})", map.value.map { entry -> entry.mappedPlaceholders }, true)
            }.toMutableList())
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun getResponses(report: String, serverCrash: Boolean): MutableMap<String, List<String>> {
        val issues = skyclientData ?: return linkedMapOf()
        val responses = linkedMapOf<String, ArrayList<String>>()

        val triggersToIgnore = arrayListOf<Int>()

        val fixTypes = issues["fixtypes"].asJsonArray
        fixTypes.map { it.asJsonObject }.forEachIndexed { index, type ->
            if (!type.has("no_ingame_display") || !type["no_ingame_display"].asBoolean) {
                if ((!type.has("server_crashes") || !type["server_crashes"].asBoolean)) {
                    if (serverCrash) {
                        triggersToIgnore.add(index)
                    }
                } else {
                    if (!serverCrash) {
                        triggersToIgnore.add(index)
                    }
                }
            } else {
                triggersToIgnore.add(index)
            }

            responses[type["name"].asString] = arrayListOf()
        }

        val fixes = issues["fixes"].asJsonArray
        val responseCategories = ArrayList(responses.keys)

        for (solution in fixes) {
            val solutionJson = solution.asJsonObject
            if (solutionJson.has("bot_only")) continue

            val triggerNumber =
                if (solutionJson.has("fixtype")) solutionJson["fixtype"].asInt else issues["default_fix_type"].asInt
            if (triggersToIgnore.contains(triggerNumber)) {
                continue
            }

            val causes = solutionJson["causes"].asJsonArray
            var trigger = false
            for (cause in causes) {
                val causeJson = cause.asJsonObject
                var theReport = report
                if (causeJson.has("unformatted") && causeJson["unformatted"].asBoolean) {
                    theReport = EnumChatFormatting.getTextWithoutFormattingCodes(theReport) ?: theReport
                }

                when (causeJson["method"].asString) {
                    "contains" -> {
                        if (theReport.contains(causeJson["value"].asString)) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }

                    "contains_not" -> {
                        if (!theReport.contains(causeJson["value"].asString)) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }

                    "regex" -> {
                        if (theReport.contains(Regex(causeJson["value"].asString, RegexOption.IGNORE_CASE))) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }

                    "regex_not" -> {
                        if (!theReport.contains(Regex(causeJson["value"].asString, RegexOption.IGNORE_CASE))) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }
                }
            }

            if (trigger) {
                responses[responseCategories[triggerNumber]]?.add(solutionJson["fix"].asString)
            }
        }

        return responses.filterNot { it.value.isEmpty() }.toMutableMap()
    }
}

data class CrashScan(
    val solutions: MutableList<Solution>
) {
    data class Solution(
        val name: String, val solutions: List<String>, val crashReport: Boolean = false
    )
}