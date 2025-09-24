package org.polyfrost.crashpatch.mixin;

import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.gui.CrashUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = InGameCatcher.class)
public class MixinInGameCatcher_UseCrashPatchGui {

    @Unique private static CrashReport crashpatch$crashReport;

    @ModifyArg(method = "displayCrashScreen", at = @At(value = "INVOKE", target = "Lfudge/notenoughcrashes/stacktrace/CrashUtils;outputReport(Lnet/minecraft/CrashReport;Z)V"), index = 0, remap = false)
    private static CrashReport captureCrashReport(CrashReport report) {
        // Capture the crash report to be used in the CrashPatch GUI
        crashpatch$crashReport = report;
        return report;
    }

    @ModifyArg(method = "displayCrashScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", remap = true), index = 0, remap = false)
    private static Screen useCrashPatchGui(Screen par1) {
        // Use the CrashPatch GUI instead of the default one
        return new CrashUI(crashpatch$crashReport, CrashUI.GuiType.NORMAL).create();
    }
}
