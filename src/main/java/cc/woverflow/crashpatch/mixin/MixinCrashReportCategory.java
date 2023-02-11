package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.hooks.StacktraceDeobfuscator;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {
    @Inject(method = "getPrunedStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;", shift = At.Shift.BY, by = 2, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterGetStacktrace(int size, CallbackInfoReturnable<Integer> cir, StackTraceElement[] stackTrace) {
        StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(stackTrace);
    }
}
