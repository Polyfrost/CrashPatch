package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.crashes.StateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO: this could be completely useless, check with smarter people
@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher implements StateManager.IResettable {
    private boolean drawingBatch = false;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        if (drawingBatch) drawingBatch = false;
    }

    @Redirect(method = "renderTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isNotFastRenderOrDrawing(TileEntity instance) {
        if (!drawingBatch) {
            return false;
        } else {
            return instance.hasFastRenderer();
        }
    }

    @Redirect(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isFastRenderOrDrawing(TileEntity instance) {
        return drawingBatch && instance.hasFastRenderer();
    }

    @Inject(method = "preDrawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchTrue(CallbackInfo ci) {
        drawingBatch = true;
    }

    @Inject(method = "drawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchFalse(int pass, CallbackInfo ci) {
        drawingBatch = false;
    }

    @Override
    public void register() {
    }
}
