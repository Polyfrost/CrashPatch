package org.polyfrost.crashpatch.client

import gs.mclo.api.APIException
import gs.mclo.api.Log
import gs.mclo.api.MclogsClient
import org.polyfrost.crashpatch.CrashPatchConstants
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

object LogUploader {
    private val mcLogsClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MclogsClient("CrashPatch", CrashPatchConstants.VERSION, "1.8.9")
    }

    private val SESSION_ID = Regex("((Session ID is|--accessToken|Your new API key is) (\\S+))")

    @JvmStatic
    fun upload(text: String): String {
        val log = Log(sanitize(text))
        return when (CrashPatchConfig.crashLogUploadMethod) {
            CrashPatchConfig.UploadMethod.HASTEBIN -> uploadToHastebin(log.content)
            CrashPatchConfig.UploadMethod.MCLOGS -> uploadToMclogs(log)
        }
    }

    private fun sanitize(text: String): String {
        var sanitizedText = text
        sanitizedText = sanitizedText.replace(SESSION_ID, "[SENSITIVE INFORMATION]")
        return sanitizedText
    }

    private fun uploadToHastebin(text: String): String {
        val postData = text.toByteArray(StandardCharsets.UTF_8)
        val connection = (URI.create("https://hst.sh/documents").toURL().openConnection() as HttpsURLConnection).apply {
            doOutput = true
            instanceFollowRedirects = false
            requestMethod = "POST"
            useCaches = false
            setRequestProperty("User-Agent", "CrashPatch/${CrashPatchConstants.VERSION}")
            setRequestProperty("Content-Length", postData.size.toString())
        }

        var response: String
        DataOutputStream(connection.outputStream).use { dos ->
            dos.write(postData)
            BufferedReader(InputStreamReader(connection.inputStream)).use { br ->
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
            mcLogsClient.uploadLog(log).url
        } catch (e: APIException) {
            e.printStackTrace()
            "Failed to upload crash log to mclo.gs"
        }
    }
}