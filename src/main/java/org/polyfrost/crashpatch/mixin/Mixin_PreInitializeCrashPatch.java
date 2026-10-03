package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_PreInitializeCrashPatch {
    //? if > 1.8.9 {
    @Inject(
            method = "<init>",
            at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/resources/ResourceManager;)Lnet/minecraft/client/renderer/texture/TextureManager;")
    )
    //?} else {
    /*@Inject(
            method = "init",
            at = @At(value = "NEW", target = "(Lnet/minecraft/client/resource/manager/ResourceManager;)Lnet/minecraft/client/render/texture/TextureManager;")
    )
    *///?}
    private void preInitialize(CallbackInfo ci) {
        CrashPatchClient.preInitialize();
    }
}
