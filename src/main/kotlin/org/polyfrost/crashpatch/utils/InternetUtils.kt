package org.polyfrost.crashpatch.utils

import org.polyfrost.crashpatch.CrashPatch
import org.polyfrost.crashpatch.config.CrashPatchConfig
import gs.mclo.api.APIException
import gs.mclo.api.Log
import gs.mclo.api.MclogsClient
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection


object InternetUtils {
    private val sessionIdRegex = Regex("((Session ID is|--accessToken|Your new API key is) (?:\\S+))")
    fun upload(text: String): String {
        val log = Log(text.replace(sessionIdRegex, "[SENSITIVE INFORMATION]"))
        return when (CrashPatchConfig.crashLogUploadMethod) {
            0 -> uploadToHastebin(log.content)
            1 -> uploadToMclogs(log)
            else -> uploadToHastebin(log.content)
        }
    }

    private fun uploadToHastebin(text: String): String {
        val postData: ByteArray = text.toByteArray(StandardCharsets.UTF_8)
        val postDataLength = postData.size

        val requestURL = "https://hst.sh/documents"
        val url = URL(requestURL)
        val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        conn.doOutput = true
        conn.instanceFollowRedirects = false
        conn.requestMethod = "POST"
        conn.setRequestProperty("User-Agent", "CrashPatch/${CrashPatch.VERSION}")
        conn.setRequestProperty("Content-Length", postDataLength.toString())
        conn.useCaches = false

        var response: String
        DataOutputStream(conn.outputStream).use { dos ->
            dos.write(postData)
            BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                response = br.readLine()
            }
        }

        if (response.contains("\"key\"")) {
            response = response.substring(response.indexOf(":") + 2, response.length - 2)
            response = "https://hst.sh/$response"
        }

        return response
    }

    private fun uploadToMclogs(log: Log): String {
        val mcLogs = MclogsClient("CrashPatch", CrashPatch.VERSION, "1.8.9")
        return try {
            val response = mcLogs.uploadLog(log)
            response.url
        } catch (e: APIException) {
            e.printStackTrace()
            "Failed to upload crash log to mclo.gs"
        }
    }
}