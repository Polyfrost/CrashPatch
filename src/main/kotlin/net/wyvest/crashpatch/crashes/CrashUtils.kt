package net.wyvest.crashpatch.crashes

import gg.essential.universal.UDesktop.open
import net.minecraft.client.Minecraft
import net.minecraft.crash.CrashReport
import net.wyvest.crashpatch.CrashPatch
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

object CrashUtils {

    fun outputReport(report: CrashReport) {
        try {
            if (report.file == null) {
                var reportName = "crash-"
                reportName += SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Date())
                reportName += if (Minecraft.getMinecraft().isCallingFromMinecraftThread) "-client" else "-server"
                reportName += ".txt"
                val reportsDir =
                    if (isClient) File(Minecraft.getMinecraft().mcDataDir, "crash-reports") else File("crash-reports")
                val reportFile = File(reportsDir, reportName)
                report.saveToFile(reportFile)
            }
        } catch (e: Throwable) {
            CrashPatch.logger.fatal("Failed saving report", e)
        }
        CrashPatch.logger.fatal(
            "Minecraft ran into a problem! " + (if (report.file != null) "Report saved to: " + report.file else "Crash report could not be saved.")
                    + "\n" + report.completeReport
        )
    }

    private val isClient: Boolean
        get() {
            return try {
                Minecraft.getMinecraft() != null
            } catch (e: NoClassDefFoundError) {
                false
            }
        }

    @Throws(IOException::class)
    fun openCrashReport(crashReport: CrashReport) {
        val report = crashReport.file
        if (report.exists()) {
            open(report)
        }
    }

    @Throws(IOException::class)
    fun uploadToUbuntuPastebin(url: String, crashReport: String): String {
        val pasteUrl = URL(url)
        val connection = pasteUrl.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = "POST"
        connection.useCaches = false
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doInput = true
        connection.doOutput = true
        connection.readTimeout = 1000000
        connection.connectTimeout = 500000
        val out = PrintWriter(connection.outputStream)
        val params = "poster=CrashReport&syntax=text&content=" + URLEncoder.encode(crashReport, "UTF-8")
        out.write(params)
        out.flush()
        var resultUrl: String?
        resultUrl = connection.getHeaderField("Location")
        if (resultUrl == null) {
            resultUrl = connection.getHeaderField("location")
        }
        return url + resultUrl
    }
}