package cc.woverflow.crashpatch.crashes

import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.logger
import cc.woverflow.crashpatch.utils.asJsonObject
import cc.woverflow.crashpatch.utils.get
import cc.woverflow.crashpatch.utils.keys
import com.google.gson.JsonArray
import gg.essential.api.utils.WebUtil
import java.util.regex.Pattern

object CrashHelper {

    private val RAM_REGEX = Pattern.compile("-Xmx(?<ram>\\d+)(?<type>[GMK])", Pattern.CASE_INSENSITIVE)

    @JvmStatic
    fun scanReport(report: String): CrashScan? {
        try {
            val responses = if (!CrashPatch.useOldRepo) getResponses(report) else getOldResponses(report)

            if (responses.isEmpty()) return null
            return CrashScan(responses)
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    private fun getResponses(report: String): Map<String, ArrayList<String>> {
        val issues = WebUtil.fetchString("https://raw.githubusercontent.com/SkyblockClient/CrashData/main/crashes.json")?.asJsonObject() ?: return emptyMap()
        val responses = linkedMapOf<String, ArrayList<String>>()

        val fixTypes = issues["fixtypes"].asJsonArray
        for (type in fixTypes) {
            responses[type.asJsonObject["name"].asString] = arrayListOf()
        }

        val fixes = issues["fixes"].asJsonArray

        for (solution in fixes) {
            val solutionJson = solution.asJsonObject
            val causes = solutionJson["causes"].asJsonArray
            var trigger = false
            for (cause in causes) {
                val causeJson = cause.asJsonObject
                when (causeJson["method"].asString) {
                    "contains" -> {
                        if (report.contains(causeJson["value"].asString)) {
                            trigger = true
                        }
                    }
                    "regex" -> {
                        if (Pattern.compile(causeJson["value"].asString, Pattern.CASE_INSENSITIVE).matcher(report).find()) {
                            trigger = true
                        }
                    }
                }
            }
            if (trigger) {
                responses[ArrayList(responses.keys)[if (solutionJson.has("fixtype")) solutionJson["fixtype"].asInt else 0]]?.add(solutionJson["fix"].asString)
            }
        }
        return responses
    }


    private fun getOldResponses(report: String): Map<String, ArrayList<String>> {
        logger.warn("Using the isXander MinecraftIssues repo is not supported! Use at your own risk.")
        val issues = WebUtil.fetchString("https://raw.githubusercontent.com/isXander/MinecraftIssues/main/issues.json")?.asJsonObject() ?: return linkedMapOf()
        val responses = linkedMapOf<String, ArrayList<String>>()

        for (category in issues.keys) {
            for (categoryElement in issues[category, JsonArray()]!!) {
                val issue = categoryElement.asJsonObject
                var info = issue["info", ""]!!
                if (info.isEmpty() && !issue.has("hardcode")) continue

                var andCheck = true
                var orCheck = true

                if (issue.has("hardcode")) {
                    when (issue["hardcode", "unknown"]!!.lowercase()) {
                        "ram" -> {
                            val matcher = RAM_REGEX.matcher(report)
                            if (matcher.find()) {
                                var ram = Integer.parseInt(matcher.group("ram"))
                                val type = matcher.group("type")
                                if (type.equals("G", true)) ram *= 1024
                                if (type.equals("K", true)) ram /= 1000
                                if (ram > 4096) info =
                                    "You are using more than 4GB of ram. This can cause issues and is generally un-needed - even on high-end PCs."

                            }
                        }
                    }
                } else {
                    for (checkElement in issue["and", JsonArray()]!!) {
                        val check = checkElement.asJsonObject

                        var outcome = true
                        when (check["method", "contains"]!!.lowercase()) {
                            "contains" -> outcome = report.contains(check["value", "ouughaughaygajhgajhkgahjk"]!!) // hi lily
                            "regex" -> outcome =
                                Pattern.compile(check["value", "ouughaughaygajhgajhkgahjk"]!!, Pattern.CASE_INSENSITIVE)
                                    .matcher(report).find()
                        }
                        if (check["not", false]) outcome = !outcome

                        if (!outcome) {
                            andCheck = false
                            break
                        }
                    }

                    if (issue.has("or")) {
                        orCheck = false
                        for (checkElement in issue["or", JsonArray()]!!) {
                            val check = checkElement.asJsonObject

                            var outcome = true
                            when (check["method", "contains"]!!.lowercase()) {
                                "contains" -> outcome = report.contains(check["value", "ouughaughaygajhgajhkgahjk"]!!)
                                "regex" -> outcome = Pattern.compile(
                                    check["value", "ouughaughaygajhgajhkgahjk"]!!,
                                    Pattern.CASE_INSENSITIVE
                                ).matcher(report).find()
                            }
                            if (check["not", false]) outcome = !outcome

                            if (outcome) {
                                orCheck = true
                                break
                            }
                        }
                    }
                }

                if (andCheck && orCheck) {
                    responses.putIfAbsent(category, arrayListOf())
                    responses[category]!!.add(info)
                }
            }
        }

        return responses
    }

}

data class CrashScan(
    val solutions: Map<String, MutableList<String>>
)