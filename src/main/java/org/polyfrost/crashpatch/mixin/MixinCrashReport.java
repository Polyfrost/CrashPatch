/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/MixinCrashReport.java
 *The source file uses the MIT License.
 */

package org.polyfrost.crashpatch.mixin;

import org.polyfrost.crashpatch.identifier.ModIdentifier;
import org.polyfrost.crashpatch.hooks.CrashReportHook;
import net.minecraft.crash.CrashReport;
import org.polyfrost.crashpatch.identifier.ModMetadata;
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
        ModMetadata susMod = ModIdentifier.INSTANCE.identifyFromStacktrace((CrashReport) (Object) this, this.cause);
        crashpatch$suspectedMod = (susMod == null ? "Unknown" : susMod.getName());
    }

    //#if MC<1.13
    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void beforePopulateEnvironment(CallbackInfo ci) {
        org.polyfrost.crashpatch.hooks.StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(this.cause);
    }
    //#endif
}
