package net.wyvest.crashpatch.crashes

import com.google.common.io.Resources
import com.google.gson.JsonArray
import net.wyvest.crashpatch.utils.JsonObjectExt
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object CrashHelper {

    private val RAM_REGEX = Pattern.compile("-Xmx(?<ram>\\d+)(?<type>[GMK])", Pattern.CASE_INSENSITIVE)

    @JvmStatic
    fun scanReport(report: String): CrashScan? {
        try {
            val responses = getResponses(report)

            if (responses.isEmpty()) return null
            val theScan = CrashScan(arrayListOf(), arrayListOf(), arrayListOf(), responses)
            responses.forEach { (t, u) ->
                when (t) {
                    "Warnings" -> {
                        theScan.warnings.addAll(u)
                    }
                    "Solutions" -> {
                        theScan.solutions.addAll(u)
                    }
                    "Recommendations" -> {
                        theScan.recommendations.addAll(u)
                    }
                }
            }
            return theScan
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    private fun getResponses(report: String): LinkedHashMap<String, ArrayList<String>> {
        @Suppress("UnstableApiUsage")
        val issues = JsonObjectExt(
            Resources.toString(
                URL("https://raw.githubusercontent.com/isXander/MinecraftIssues/main/issues.json"),
                StandardCharsets.UTF_8
            )
        )
        val responses = linkedMapOf<String, ArrayList<String>>()

        for (category in issues.keys) {
            for (categoryElement in issues[category, JsonArray()]!!) {
                val issue = JsonObjectExt(categoryElement.asJsonObject)
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
                        val check = JsonObjectExt(checkElement.asJsonObject)

                        var outcome = true
                        when (check["method", "contains"]!!.lowercase()) {
                            "contains" -> outcome = report.contains(check["value", "ouughaughaygajhgajhkgahjk"]!!)
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
                            val check = JsonObjectExt(checkElement.asJsonObject)

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
    val warnings: ArrayList<String>,
    val solutions: ArrayList<String>,
    val recommendations: ArrayList<String>,
    val responses: LinkedHashMap<String, ArrayList<String>>
)