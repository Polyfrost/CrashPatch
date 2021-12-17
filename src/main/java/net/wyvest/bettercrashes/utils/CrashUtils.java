/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/CrashUtils.java
 *The source file uses the MIT License.
 */

package net.wyvest.bettercrashes.utils;

import gg.essential.universal.UDesktop;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.wyvest.bettercrashes.BetterCrashes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Runemoro
 */
public class CrashUtils {

    /**
     * @author Runemoro
     */
    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getMinecraft().isCallingFromMinecraftThread() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = isClient() ? new File(Minecraft.getMinecraft().mcDataDir, "crash-reports") : new File("crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.saveToFile(reportFile);
            }
        } catch (Throwable e) {
            BetterCrashes.logger.fatal("Failed saving report", e);
        }

        BetterCrashes.logger.fatal("Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile() : "Crash report could not be saved.")
                + "\n" + report.getCompleteReport());
    }

    /**
     * @author Runemoro
     */
    private static boolean isClient() {
        try {
            return Minecraft.getMinecraft() != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static void openCrashReport(CrashReport crashReport) throws IOException {
        File report = crashReport.getFile();
        if (report.exists()) {
            UDesktop.open(report);
        }
    }

}
