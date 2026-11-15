package org.polyfrost.crashpatch.client

import gs.mclo.api.Log
import gs.mclo.api.MclogsClient
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.oneconfig.utils.v1.NetworkUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException

object LogUploader {
    private val mcLogsClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MclogsClient("CrashPatch", CrashPatchConstants.VERSION, "1.8.9")
    }

    private val SESSION_ID = Regex("((Session ID is|--accessToken|Your new API key is) (\\S+))")

    @JvmStatic
    fun upload(text: String): String? {
        val sanitized = sanitize(text)
        return when (CrashPatchConfig.crashLogUploadMethod) {
            CrashPatchConfig.UploadMethod.HASTEBIN -> uploadToHastebin(sanitized)
            CrashPatchConfig.UploadMethod.MCLOGS -> uploadToMclogs(Log(sanitized))
        }
    }

    private fun sanitize(text: String): String {
        var sanitizedText = text
        sanitizedText = sanitizedText.replace(SESSION_ID, "[SENSITIVE INFORMATION]")
        return sanitizedText
    }

    private fun uploadToHastebin(text: String): String? {
        val response = NetworkUtils.post(
            "https://hst.sh/documents",
            text.toByteArray(StandardCharsets.UTF_8),
            "text/plain; charset=utf-8",
            mapOf("User-Agent" to "CrashPatch/${CrashPatchConstants.VERSION}"),
        )?.trim() ?: return null

        if (!response.contains("\"key\"")) return null
        return "https://hst.sh/" + response.substring(response.indexOf(":") + 2, response.length - 2)
    }

    private fun uploadToMclogs(log: Log): String? {
        return try {
            mcLogsClient.uploadLog(log).join().url
        } catch (e: CompletionException) {
            e.cause?.printStackTrace() ?: e.printStackTrace()
            null
        }
    }
}