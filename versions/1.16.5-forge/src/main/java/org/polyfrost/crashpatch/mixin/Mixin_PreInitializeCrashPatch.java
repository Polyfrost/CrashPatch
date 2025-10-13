package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Minecraft.class)
public class Mixin_PreInitializeCrashPatch {
    // Random injection point that works across 1.16-1.21.x
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target =
            //#if MC<1.18
            "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
            //#else
            //$$ "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
            //#endif
            , remap = false, ordinal = 0), index = 0, remap = true)
    private static String preInitialize(String par1) {
        CrashPatchClient.preInitialize();
        return par1;
    }
}
