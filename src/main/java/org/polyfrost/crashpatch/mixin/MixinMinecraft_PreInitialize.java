package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft_PreInitialize {

    @Inject(method = "startGame", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/IResourceManager;)Lnet/minecraft/client/renderer/texture/TextureManager;"))
    private void preInitialize(CallbackInfo ci) {
        CrashPatchClient.INSTANCE.preInitialize();
    }
}
