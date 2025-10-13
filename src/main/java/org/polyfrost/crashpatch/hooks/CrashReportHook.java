package org.polyfrost.crashpatch.hooks;

public interface CrashReportHook {
    String crashpatch$getSuspectedMod();

    default String getSuspectedMod() {
        return crashpatch$getSuspectedMod();
    }
}
