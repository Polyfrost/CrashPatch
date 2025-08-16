package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import org.polyfrost.crashpatch.gui.CrashUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft_CrashInitGui {

    @Inject(method = "runTick", at = @At("HEAD"))
    private void crashInitGui(boolean bl, CallbackInfo ci) throws Throwable {
        if (CrashUI.Companion.getCurrentUI() != null && CrashUI.Companion.getCurrentUI().getShouldCrash()) {
            throw Objects.requireNonNull(CrashUI.Companion.getCurrentUI().getThrowable());
        }
    }
}
