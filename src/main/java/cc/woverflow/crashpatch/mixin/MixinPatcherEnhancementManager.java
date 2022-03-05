package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.hooks.EnhancementManagerHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.util.enhancement.EnhancementManager")
public class MixinPatcherEnhancementManager implements EnhancementManagerHook {
    private boolean isTicking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!isTicking) {
            isTicking = true;
        }
    }

    @Override
    public boolean getTicking() {
        return isTicking;
    }
}
