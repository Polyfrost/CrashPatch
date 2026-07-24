package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.crashpatch.client.utils.DisconnectedScreenHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if < 26.2 {
/*@Mixin(Minecraft.class)
*///? } else {
@Mixin(net.minecraft.client.gui.Gui.class)
//? }
public class Mixin_OverrideDisconnectedScreen {
    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGUIDisplay(Screen screen, CallbackInfo ci) {
        DisconnectedScreenHook.onScreenDisplayed(screen, ci);
    }
}
