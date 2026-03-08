package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.client.utils.DisconnectedScreenHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_OverrideDisconnectedScreen {
    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGUIDisplay(Screen i, CallbackInfo ci) {
        DisconnectedScreenHook.onScreenDisplayed(i, ci);
    }
}
