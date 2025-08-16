package org.polyfrost.crashpatch.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.gui.CrashUI;
import org.polyfrost.oneconfig.api.ui.v1.internal.wrappers.PolyUIScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 1500)
public class MixinMinecraft_CrashPatchInitUI {

    /**
     * If the game has crashed, we set the screen to the init crash screen, but then Minecraft sets the screen back
     * to the title screen. We want to prevent that, to keep the screen to be the CrashUI
     */
    @TargetHandler(
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

    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setScreenDontResetCrashScreen(Screen screen, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() && !(screen instanceof PolyUIScreen && screen == CrashUI.Companion.getCurrentInstance())) {
            ci.cancel();
        }
    }
}
