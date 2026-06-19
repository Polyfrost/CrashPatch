package org.polyfrost.crashpatch.mixin;

import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameCatcher.class, remap = false)
public class Mixin_InGameCatcherKeepWorld {

    @Unique private static final long CRASH_WINDOW_MS = 10_000L;

    @Unique private static long crashpatch$keptAliveAt = 0L;

    @Unique private static Boolean crashpatch$latchedDecision = null;

    @Inject(method = "cleanupBeforeMinecraft", at = @At("HEAD"), cancellable = true)
    private static void crashpatch$keepWorldOnCleanup(CallbackInfo ci) {
        boolean keep = crashpatch$decideKeepWorld();
        crashpatch$latchedDecision = keep;
        if (keep) ci.cancel();
    }

    @Inject(method = "resetCriticalGameState", at = @At("HEAD"), cancellable = true)
    private static void crashpatch$keepWorldOnReset(CallbackInfo ci) {
        boolean keep;
        if (crashpatch$latchedDecision != null) {
            keep = crashpatch$latchedDecision;
            crashpatch$latchedDecision = null;
        } else {
            keep = crashpatch$decideKeepWorld();
        }
        if (keep) ci.cancel();
    }

    @Unique
    private static boolean crashpatch$decideKeepWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hasSingleplayerServer()) return false;

        long now = System.currentTimeMillis();
        if (crashpatch$keptAliveAt != 0L && now - crashpatch$keptAliveAt <= CRASH_WINDOW_MS) {
            crashpatch$keptAliveAt = 0L;
            return false;
        }

        crashpatch$keptAliveAt = now;
        return true;
    }
}
