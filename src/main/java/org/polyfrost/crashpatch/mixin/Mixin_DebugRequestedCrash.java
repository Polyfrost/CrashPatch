package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_DebugRequestedCrash {
    @Inject(method = "runTick", at = @At("RETURN"))
    private void debugCrash(CallbackInfo ci) {
        if (CrashPatchClient.isCrashRequested()) {
            CrashPatchClient.setCrashRequested(false);
            throw new RuntimeException("Crash requested by CrashPatch");
        }
    }
}
