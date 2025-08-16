package org.polyfrost.crashpatch.utils

import gs.mclo.api.APIException
import gs.mclo.api.Log
import gs.mclo.api.MclogsClient
import org.polyfrost.crashpatch.CrashPatchConfig
import org.polyfrost.crashpatch.CrashPatchConstants
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

object UploadUtils {

    private val mclogsClient by lazy {
        MclogsClient("CrashPatch", CrashPatchConstants.VERSION, "1.8.9")
    }

    private val sessionIdRegex = Regex("((Session ID is|--accessToken|Your new API key is) (\\S+))")

    fun upload(text: String): String {
        val sanitizedText = text.replace(sessionIdRegex, "[SENSITIVE INFORMATION]")
        val log = Log(sanitizedText)
        return when (CrashPatchConfig.crashLogUploadMethod) {
            CrashPatchConfig.UploadMethod.HASTEBIN -> uploadToHastebin(log.content)
            CrashPatchConfig.UploadMethod.MCLOGS -> uploadToMclogs(log)
        }
    }

    private fun uploadToHastebin(text: String): String {
        val postData: ByteArray = text.toByteArray(StandardCharsets.UTF_8)
        val postDataLength = postData.size

        val requestURL = "https://hst.sh/documents"
        val url = URI.create(requestURL).toURL()
        val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        conn.doOutput = true
        conn.instanceFollowRedirects = false
        conn.requestMethod = "POST"
        conn.setRequestProperty("User-Agent", "CrashPatch/${CrashPatchConstants.VERSION}")
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
        return try {
            mclogsClient.uploadLog(log).url
        } catch (e: APIException) {
            e.printStackTrace()
            "Failed to upload crash log to mclo.gs"
        }
    }

}