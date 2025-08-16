package org.polyfrost.crashpatch.mixin;

import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {
    //#if MC<1.13
    @Inject(method = "getPrunedStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;", shift = At.Shift.BY, by = 2, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterGetStacktrace(int size, CallbackInfoReturnable<Integer> cir, StackTraceElement[] stackTrace) {
        org.polyfrost.crashpatch.hooks.StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(stackTrace);
    }
    //#endif
}
