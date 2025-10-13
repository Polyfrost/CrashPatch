package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.polyfrost.crashpatch.client.utils.DisconnectedScreenHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_OverrideDisconnectedScreen {
    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    private void onGUIDisplay(GuiScreen i, CallbackInfo ci) {
        DisconnectedScreenHook.onScreenDisplayed(i, ci);
    }
}
