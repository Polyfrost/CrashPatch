package cc.woverflow.crashpatch.utils

import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import gg.essential.api.utils.WebUtil
import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.gui.GuiDownload
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object Updater {
    var latestTag = ""
    var shouldUpdate = false
    var updateUrl = ""

    /**
     * Stolen from SimpleTimeChanger under AGPLv3
     * https://github.com/My-Name-Is-Jeff/SimpleTimeChanger/blob/master/LICENSE
     */
    fun update() {
        Multithreading.runAsync {
            val latestRelease = WebUtil.fetchString("https://api.github.com/repos/W-OVERFLOW/${CrashPatch.MODID}/releases/latest")?.asJsonObject() ?: return@runAsync
            latestTag = latestRelease["tag_name"].asString

            val currentVersion = CrashPatchVersion.CURRENT
            latestTag = latestRelease["tag_name"].asString.substringAfter("v")
            val latestVersion = CrashPatchVersion.fromString(latestTag)
            if (currentVersion < latestVersion) {
                updateUrl = latestRelease["assets"].asJsonArray[0].asJsonObject["browser_download_url"].asString
                shouldUpdate = true
                EssentialAPI.getNotifications().push("Mod Update", "${CrashPatch.NAME} $latestTag is available!\nClick here to download it!") {
                    EssentialAPI.getGuiUtil().openScreen(GuiDownload())
                }
            }
        }
    }

    /**
     * Adapted from RequisiteLaunchwrapper under LGPLv3
     * https://github.com/Qalcyo/RequisiteLaunchwrapper/blob/main/LICENSE
     */
    fun download(url: String, file: File): Boolean {
        if (file.exists()) return true
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        var newUrl = url
        newUrl = newUrl.replace(" ", "%20")
        val downloadClient: HttpClient =
            HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(10000).build())
                .build()
        try {
            FileOutputStream(file).use { fileOut ->
                val downloadResponse: HttpResponse = downloadClient.execute(HttpGet(newUrl))
                val buffer = ByteArray(1024)
                var read: Int
                while (downloadResponse.entity.content.read(buffer).also { read = it } > 0) {
                    fileOut.write(buffer, 0, read)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * Adapted from Skytils under AGPLv3
     * https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md
     */
    fun addShutdownHook() {
        EssentialAPI.getShutdownHookUtil().register {
            println("Deleting old ${CrashPatch.NAME} jar file...")
            try {
                val runtime = getJavaRuntime()
                if (System.getProperty("os.name").lowercase(Locale.ENGLISH).contains("mac")) {
                    val sipStatus = Runtime.getRuntime().exec("csrutil status")
                    sipStatus.waitFor()
                    if (!sipStatus.inputStream.use { it.bufferedReader().readText() }
                            .contains("System Integrity Protection status: disabled.")) {
                        println("SIP is NOT disabled, opening Finder.")
                        Desktop.getDesktop().open(CrashPatch.jarFile.parentFile)
                    }
                }
                println("Using runtime $runtime")
                val file = File(CrashPatch.modDir.parentFile, "Deleter-1.2.jar")
                println("\"$runtime\" -jar \"${file.absolutePath}\" \"${CrashPatch.jarFile.absolutePath}\"")
                if (System.getProperty("os.name").lowercase(Locale.ENGLISH).containsAny("linux", "unix")) {
                    println("On Linux, giving Deleter jar execute permissions...")
                    Runtime.getRuntime()
                        .exec("chmod +x \"${file.absolutePath}\"")
                }
                Runtime.getRuntime()
                    .exec("\"$runtime\" -jar \"${file.absolutePath}\" \"${CrashPatch.jarFile.absolutePath}\"")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Gets the current Java runtime being used.
     * @link https://stackoverflow.com/a/47925649
     */
    @Throws(IOException::class)
    fun getJavaRuntime(): String {
        val os = System.getProperty("os.name")
        val java = "${System.getProperty("java.home")}${File.separator}bin${File.separator}${
            if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java"
        }"
        if (!File(java).isFile) {
            throw IOException("Unable to find suitable java runtime at $java")
        }
        return java
    }
}