package cc.woverflow.crashpatch.hooks;

import net.minecraftforge.fml.common.ModContainer;

import java.util.Set;

public interface CrashReportHook {
    Set<ModContainer> getSuspectedMods();
}
