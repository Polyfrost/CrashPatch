package cc.woverflow.crashpatch.crashes

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent
import cc.polyfrost.oneconfig.utils.NetworkUtils
import com.google.gson.JsonObject
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
        try {
            val responses = getResponses(report, serverCrash)

            if (responses.isEmpty()) return null
            return CrashScan(responses)
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    private fun getResponses(report: String, serverCrash: Boolean): Map<String, ArrayList<String>> {
        val issues = skyclientJson ?: return emptyMap()
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
                if (serverCrash && causeJson.has("unformatted") && causeJson["unformatted"].asBoolean) {
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
        return responses.filterNot { it.value.isEmpty() }
    }
}

data class CrashScan(
    val solutions: Map<String, MutableList<String>>
)