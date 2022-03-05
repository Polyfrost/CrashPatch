package cc.woverflow.crashpatch.hooks;

import net.minecraft.client.resources.IReloadableResourceManager;

public interface MinecraftHook {
    boolean hasRecoveredFromCrash();

    IReloadableResourceManager getResourceManager();
}
