package org.polyfrost.crashpatch.mixin;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
//? if >=1.21.11 {
import net.minecraft.util.Util;
//? } else {
/*import net.minecraft.Util;
*///? }
import org.apache.logging.log4j.LogManager;
import org.polyfrost.crashpatch.CrashPatchConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletionException;

@Mixin(Util.class)
public class Mixin_RelayWorkerThreadCrash {
    @Inject(method = "onThreadException", at = @At("HEAD"), cancellable = true)
    private static void crashpatch$relayWorkerCrash(Thread thread, Throwable thrown, CallbackInfo ci) {
        Throwable cause = thrown instanceof CompletionException ? thrown.getCause() : thrown;
        if (!(cause instanceof ReportedException)) return;

        CrashReport report = ((ReportedException) cause).getReport();
        report.addCategory("ThreadInfo").setDetail("Name", thread.getName());
        LogManager.getLogger(CrashPatchConstants.NAME).error("Caught exception in thread {}", thread, cause);

        Minecraft client = Minecraft.getInstance();
        if (NotEnoughCrashes.enableGameloopCatching() && client != null && client.isRunning()) {
            client.delayCrash(report);
            ci.cancel();
            return;
        }

        CrashUtils.outputReport(report, true);
    }
}
