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
    /**
     * If the game has crashed, we set the screen to the init crash screen, but then Minecraft sets the screen back
     * to the title screen. We want to prevent that, to keep the screen to be the CrashUI.
     * <p>
     * Pre-26.2 the reset flows through {@code Minecraft#setScreen}, and NEC's own MixinMinecraftClient guards it,
     * so we disable NEC's guard (below) and run our own. From 26.2 screen management moved to {@code Gui#setScreen}
     * and NEC no longer guards it, so this mixin targets {@code Gui} directly and the NEC handler override is dropped.
     */
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
