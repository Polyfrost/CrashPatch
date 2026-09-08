package org.polyfrost.crashpatch.mixin;

import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameCatcher.class, remap = false)
public class Mixin_LogCrashWhenScreenSkipped {
    @Inject(method = "displayCrashScreen", at = @At("HEAD"))
    private static void crashpatch$reportBeforeBailing(CrashReport report, int crashCount, boolean clientCrash, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() || crashCount > NecConfig.getCurrent().crashLimit()) {
            CrashUtils.outputReport(report, clientCrash);
        }
    }
}
