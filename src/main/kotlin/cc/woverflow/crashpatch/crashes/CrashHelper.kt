package cc.woverflow.crashpatch.crashes

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent
import cc.polyfrost.oneconfig.utils.NetworkUtils
import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.hooks.McDirUtil
import com.google.gson.JsonObject
import net.minecraft.launchwrapper.Launch
import java.io.File
import kotlin.collections.set

object CrashHelper {

    private var skyclientJson: JsonObject? = null

    @JvmStatic
    fun loadJson(): Boolean {
        return try {
            skyclientJson =
                NetworkUtils.getJsonElement("https://raw.githubusercontent.com/SkyblockClient/CrashData/main/crashes.json").asJsonObject
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun scanReport(report: String, serverCrash: Boolean = false): CrashScan? {
        return try {
            val responses = getResponses(report, serverCrash)
            CrashScan(responses.also { it["Crash log"] = report.split("\\R".toRegex()) }.toSortedMap { o1, o2 ->
                if (o1 == "Crash log") {
                    return@toSortedMap 1
                }
                if (o2 == "Crash log") {
                    return@toSortedMap -1
                }
                return@toSortedMap o1.compareTo(o2)
            }.map { map ->
                CrashScan.Solution("${map.key} (${map.value.size})", map.value.map {
                    it.replace("%pathindicator%", "").replace(
                        "%gameroot%", CrashPatch.gameDir.absolutePath.removeSuffix(
                            File.separator
                        )
                    ).replace("%profileroot%", File(McDirUtil.getMcDir(), "OneConfig").parentFile.absolutePath.removeSuffix(File.separator))
                }, true)
            }.toMutableList())
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun getResponses(report: String, serverCrash: Boolean): MutableMap<String, List<String>> {
        val issues = skyclientJson ?: return linkedMapOf()
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
                    theReport = UTextComponent.stripFormatting(theReport)
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