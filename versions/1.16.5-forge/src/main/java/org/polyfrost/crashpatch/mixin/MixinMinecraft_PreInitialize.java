package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MixinMinecraft_PreInitialize {

    @ModifyVariable(method = "<init>", at = @At("STORE"), argsOnly = true, ordinal = 0)
    private static GameConfig preInitialize(GameConfig v) {
        CrashPatchClient.INSTANCE.preInitialize();
        return v;
    }
}
