package cc.woverflow.crashpatch.crashes

import cc.woverflow.crashpatch.utils.asJsonObject
import com.google.gson.JsonObject
import gg.essential.api.utils.WebUtil
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.arrayListOf
import kotlin.collections.emptyMap
import kotlin.collections.filterNot
import kotlin.collections.forEachIndexed
import kotlin.collections.linkedMapOf
import kotlin.collections.map
import kotlin.collections.set

object CrashHelper {

    private var skyclientJson: JsonObject? = null

    @JvmStatic
    fun loadJson(): Boolean {
        return try {
            skyclientJson = WebUtil.fetchString("https://raw.githubusercontent.com/SkyblockClient/CrashData/main/crashes.json")?.asJsonObject()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun scanReport(report: String): CrashScan? {
        try {
            val responses = getResponses(report)

            if (responses.isEmpty()) return null
            return CrashScan(responses)
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    private fun getResponses(report: String): Map<String, ArrayList<String>> {
        val issues = skyclientJson ?: return emptyMap()
        val responses = linkedMapOf<String, ArrayList<String>>()

        val triggersToIgnore = arrayListOf<Int>()

        val fixTypes = issues["fixtypes"].asJsonArray
        fixTypes.map { it.asJsonObject }.forEachIndexed { index, type ->
            if (!type.has("no_ingame_display") || !type["no_ingame_display"].asBoolean) {
                responses[type["name"].asString] = arrayListOf()
            } else {
                triggersToIgnore.add(index)
            }
        }

        val fixes = issues["fixes"].asJsonArray
        val responseCategories = ArrayList(responses.keys)

        for (solution in fixes) {
            val solutionJson = solution.asJsonObject
            if (solutionJson.has("bot_only")) continue
            val triggerNumber = if (solutionJson.has("fixtype")) solutionJson["fixtype"].asInt else issues["default_fix_type"].asInt
            if (triggersToIgnore.contains(triggerNumber)) {
                continue
            }
            val causes = solutionJson["causes"].asJsonArray
            var trigger = false
            for (cause in causes) {
                val causeJson = cause.asJsonObject
                when (causeJson["method"].asString) {
                    "contains" -> {
                        if (report.contains(causeJson["value"].asString)) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }
                    "contains_not" -> {
                        if (!report.contains(causeJson["value"].asString)) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }
                    "regex" -> {
                        if (report.contains(Regex(causeJson["value"].asString, RegexOption.IGNORE_CASE))) {
                            trigger = true
                        } else {
                            trigger = false
                            break
                        }
                    }
                    "regex_not" -> {
                        if (!report.contains(Regex(causeJson["value"].asString, RegexOption.IGNORE_CASE))) {
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