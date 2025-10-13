package org.polyfrost.crashpatch.mixin;

//#if FORGE && MC < 1.13
import org.polyfrost.crashpatch.client.crashes.GameStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.12.2
//$$ import org.spongepowered.asm.mixin.Shadow;
//#endif

@Mixin(TileEntityRendererDispatcher.class)
public abstract class Mixin_TileEntityRenderer implements GameStateManager.ResettableGameObject {
    //#if MC >= 1.12.2
    //$$ @Shadow private boolean drawingBatch;
    //#else
    @Unique private boolean crashpatch$drawingBatch;
    //#endif

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        GameStateManager.register(this);
    }

    @Override
    public void crashpatch$resetGameState() {
        //#if MC < 1.12
        if (crashpatch$drawingBatch) {
            crashpatch$drawingBatch = false;
        }
        //#else
        //$$ if (drawingBatch) {
        //$$     drawingBatch = false;
        //$$ }
        //#endif
    }

    //#if MC < 1.12
    @Redirect(method = "renderTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isNotFastRenderOrDrawing(TileEntity instance) {
        if (!crashpatch$drawingBatch) {
            return false;
        } else {
            return instance.hasFastRenderer();
        }
    }

    @Redirect(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isFastRenderOrDrawing(TileEntity instance) {
        return crashpatch$drawingBatch && instance.hasFastRenderer();
    }

    @Inject(method = "preDrawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchTrue(CallbackInfo ci) {
        crashpatch$drawingBatch = true;
    }

    @Inject(method = "drawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchFalse(int pass, CallbackInfo ci) {
        crashpatch$drawingBatch = false;
    }
    //#endif
}
//#endif
