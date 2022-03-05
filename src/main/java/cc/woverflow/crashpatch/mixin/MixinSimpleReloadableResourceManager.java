package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.hooks.SimpleReloadableResourceManagerHook;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Function;

@Mixin(SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager implements SimpleReloadableResourceManagerHook {

    @Shadow @Final private List<IResourceManagerReloadListener> reloadListeners;

    @Override
    public void removeReloadListener(IResourceManagerReloadListener reloadListener) {
        try {
            if (reloadListener != null) {
                reloadListeners.remove(reloadListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeIf(Function<IResourceManagerReloadListener, Boolean> function) {
        reloadListeners.removeIf(function::apply);
    }
}
