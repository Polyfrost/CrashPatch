/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/MixinCrashReport.java
 *The source file uses the MIT License.
 */

package net.wyvest.crashpatch.mixin;

import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import net.wyvest.crashpatch.crashes.ModIdentifier;
import net.wyvest.crashpatch.hooks.CrashReportHook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = CrashReport.class, priority = 500)
public class MixinCrashReport implements CrashReportHook {
    @Shadow
    @Final
    private Throwable cause;

    private Set<ModContainer> suspectedMods;

    @Override
    public Set<ModContainer> getSuspectedMods() {
        return suspectedMods;
    }

    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void afterPopulateEnvironment(CallbackInfo ci) {
        suspectedMods = ModIdentifier.INSTANCE.identifyFromStacktrace(cause);
    }
}
