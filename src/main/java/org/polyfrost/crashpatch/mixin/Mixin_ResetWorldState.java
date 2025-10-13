package org.polyfrost.crashpatch.mixin;

//#if MC < 1.13
import org.polyfrost.crashpatch.client.crashes.GameStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class Mixin_ResetWorldState implements GameStateManager.ResettableGameObject {
    @Shadow private boolean isDrawing;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitEnd(int bufferSizeIn, CallbackInfo ci) {
        GameStateManager.register(this);
    }

    @Override
    public void crashpatch$resetGameState() {
        if (this.isDrawing) {
            finishDrawing();
        }
    }

    @Shadow public abstract void finishDrawing();
}
//#endif
