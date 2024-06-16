/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/MixinCrashReport.java
 *The source file uses the MIT License.
 */

package org.polyfrost.crashpatch.mixin;

import org.polyfrost.crashpatch.crashes.ModIdentifier;
import org.polyfrost.crashpatch.hooks.CrashReportHook;
import org.polyfrost.crashpatch.hooks.StacktraceDeobfuscator;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrashReport.class, priority = 500)
public class MixinCrashReport implements CrashReportHook {
    @Shadow
    @Final
    private Throwable cause;
    private String crashpatch$suspectedMod;

    @Override
    public String getSuspectedCrashPatchMods() {
        return crashpatch$suspectedMod;
    }

    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void afterPopulateEnvironment(CallbackInfo ci) {
        ModContainer susMod = ModIdentifier.INSTANCE.identifyFromStacktrace(cause);
        crashpatch$suspectedMod = (susMod == null ? "Unknown" : susMod.getName());
    }

    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void beforePopulateEnvironment(CallbackInfo ci) {
        StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(cause);
    }
}
