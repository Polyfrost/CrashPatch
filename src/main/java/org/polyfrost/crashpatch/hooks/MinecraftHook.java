package org.polyfrost.crashpatch.hooks;

public interface MinecraftHook {
    boolean hasRecoveredFromCrash();
    void crashPatch$die();
}
