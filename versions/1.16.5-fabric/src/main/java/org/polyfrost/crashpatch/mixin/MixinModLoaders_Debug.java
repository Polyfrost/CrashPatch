package org.polyfrost.crashpatch.mixin;

//#if FABRIC
import fudge.notenoughcrashes.fabric.mixinhandlers.ModLoaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Objects;

@Mixin(ModLoaders.class)
public class MixinModLoaders_Debug {

    @Inject(method = "fabricEntrypoints", at = @At("HEAD"), remap = false)
    private static void debugEntrypointCrashes(File runDir, Object gameInstance, CallbackInfo ci) {
        if (Objects.equals(System.getProperty("polyfrost.crashpatch.init_crash"), "true")) {
            throw new RuntimeException("Crash requested by CrashPatch");
        }
    }

    @Inject(method = "quiltEntrypoints", at = @At("HEAD"), remap = false)
    private static void debugEntrypointCrashesQuilt(File runDir, Object gameInstance, CallbackInfo ci) {
        debugEntrypointCrashes(runDir, gameInstance, ci);
    }
}
//#endif