/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/MixinCrashReport.java
 *The source file uses the MIT License.
 */

package org.polyfrost.crashpatch.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.polyfrost.crashpatch.client.crashes.LogScanner;
import org.polyfrost.crashpatch.identifier.ModIdentifier;
import org.polyfrost.crashpatch.hooks.CrashReportHook;
import org.polyfrost.crashpatch.identifier.ModMetadata;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CrashReport.class, priority = 500)
public class Mixin_IdentifySuspiciousMod implements CrashReportHook {
    @Shadow @Final private Throwable exception;
    @Unique private String crashpatch$suspectedMod;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void afterPopulateEnvironment(CallbackInfo ci) {
        ModMetadata suspiciousMod = ModIdentifier.INSTANCE.identifyFromStacktrace((CrashReport) (Object) this, this.exception);
        crashpatch$suspectedMod = (suspiciousMod == null ? "Unknown" : suspiciousMod.getName());
        crashpatch$reportSuppressedErrors();
    }

    @Unique
    private void crashpatch$reportSuppressedErrors() {
        List<String> suppressed = LogScanner.reportLines();
        if (suppressed.isEmpty()) return;

        CrashReportCategory category = ((CrashReport) (Object) this).addCategory("Errors before the crash (found by CrashPatch)");
        for (int i = 0; i < suppressed.size(); i++) {
            category.setDetail("Line " + (i + 1), suppressed.get(i));
        }
    }

    @Override @Unique
    public String crashpatch$getSuspectedMod() {
        return crashpatch$suspectedMod;
    }
}
