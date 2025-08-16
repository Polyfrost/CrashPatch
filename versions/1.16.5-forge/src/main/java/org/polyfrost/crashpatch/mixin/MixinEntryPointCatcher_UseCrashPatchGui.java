package org.polyfrost.crashpatch.mixin;

import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.gui.CrashUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = EntryPointCatcher.class)
public class MixinEntryPointCatcher_UseCrashPatchGui {

    @Shadow private static CrashReport crashReport;

    @ModifyArg(method = "displayInitErrorScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"), index = 0)
    private static Screen useCrashPatchGui(Screen screen) {
        // Use the CrashPatch GUI instead of the default one
        return new CrashUI(crashReport, CrashUI.GuiType.INIT).create();
    }
}
