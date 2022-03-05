package cc.woverflow.crashpatch.tweaker;

import cc.woverflow.crashpatch.CrashPatch;
import cc.woverflow.crashpatch.CrashPatchKt;
import cc.woverflow.crashpatch.crashes.CrashHelper;
import cc.woverflow.crashpatch.crashes.DeobfuscatingRewritePolicy;
import cc.woverflow.wcore.tweaker.WCoreTweaker;
import gg.essential.api.utils.Multithreading;

public class CrashPatchWCoreTweaker extends WCoreTweaker {

    public CrashPatchWCoreTweaker() {
        super();
        DeobfuscatingRewritePolicy.Companion.install();
        Multithreading.runAsync(() -> {
            CrashPatch.INSTANCE.findMods();
            if (!CrashHelper.loadJson()) {
                CrashPatchKt.getLogger().warn("CrashHelper failed to preload crash data JSON!");
            }
        });
    }
}
