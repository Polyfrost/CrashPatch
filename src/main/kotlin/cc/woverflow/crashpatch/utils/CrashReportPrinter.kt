package cc.woverflow.crashpatch.utils

import net.minecraft.client.Minecraft
import net.minecraft.crash.CrashReport
import net.minecraft.init.Bootstrap
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CrashReportPrinter {
    fun displayCrashReport(crashReportIn: CrashReport) {
        val file1 = File(Minecraft.getMinecraft().mcDataDir, "crash-reports")
        val file2 = File(file1, "crash-" + SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Date()) + "-client.txt")
        Bootstrap.printToSYSOUT(crashReportIn.completeReport)
        if (crashReportIn.file != null) {
            Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.file)
        } else if (crashReportIn.saveToFile(file2)) {
            Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.absolutePath)
        } else {
            Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#")
        }
    }
}