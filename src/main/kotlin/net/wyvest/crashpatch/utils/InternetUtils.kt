package net.wyvest.crashpatch.utils

import net.wyvest.crashpatch.CrashPatch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

object InternetUtils {
    fun uploadToHastebin(text: String): String {
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
}