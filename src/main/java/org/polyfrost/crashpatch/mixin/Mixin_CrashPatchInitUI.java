package org.polyfrost.crashpatch.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.client.gui.CrashUI;
import org.polyfrost.oneconfig.internal.ui.compose.ComposeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if < 26.2 {
/*@Mixin(value = Minecraft.class, priority = 1500)
*///? } else {
@Mixin(value = net.minecraft.client.gui.Gui.class, priority = 1500)
//? }
public class Mixin_CrashPatchInitUI {
    // Minecraft resets the screen to the title screen after an init crash so we block that and keep the CrashUI
    // Pre 26.2 NEC guards the same reset so its handler is disabled below
    //? if < 26.2 {
    /*@TargetHandler(
            mixin = "fudge.notenoughcrashes.mixins.client.MixinMinecraftClient",
            name = "setScreenDontResetCrashScreen",
            prefix = "handler"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lfudge/notenoughcrashes/mixinhandlers/EntryPointCatcher;crashedDuringStartup()Z"
            )
    )
    private boolean setScreenDontResetCrashScreen(boolean original) {
        return false;
    }
    *///? }

    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setScreenDontResetCrashScreen(Screen screen, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() && !(screen instanceof ComposeScreen && screen == CrashUI.Companion.getCurrentInstance())) {
            ci.cancel();
        }
    }
}
