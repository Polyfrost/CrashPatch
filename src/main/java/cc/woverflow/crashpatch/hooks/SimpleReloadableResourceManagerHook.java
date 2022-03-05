package cc.woverflow.crashpatch.hooks;

import net.minecraft.client.resources.IResourceManagerReloadListener;

import java.util.function.Function;

public interface SimpleReloadableResourceManagerHook {
    void removeReloadListener(IResourceManagerReloadListener reloadListener);

    void removeIf(Function<IResourceManagerReloadListener, Boolean> function);
}
