package net.wyvest.bettercrashes.utils;

import net.minecraft.crash.CrashReport;

public interface IPatchedMinecraft {
    boolean shouldCrashIntegratedServerNextTick();

    void showWarningScreen(CrashReport report);

    void makeErrorNotification(CrashReport report);
}
