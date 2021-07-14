/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/CrashUtils.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import vfyjxf.bettercrashes.BetterCrashes;

import java.awt.*;
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
     * @param report
     */
    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getMinecraft().func_152345_ab() ? "-client" : "-server";
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

    /**
     * @author vfyjxf
     * @param crashReport
     * @throws IOException
     */
    public static void openCrashReport(CrashReport crashReport) throws IOException {

        if(!Desktop.isDesktopSupported()){
            BetterCrashes.logger.error("Desktop is not supported");
            return;
        }
        File report = crashReport.getFile();
        if(report.exists()){
            Desktop.getDesktop().open(report);
        }
    }

}
