package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.CrashPatch;
import cc.woverflow.onecore.utils.InternetUtils;
import cc.woverflow.onecore.utils.Utils;
import gg.essential.api.EssentialAPI;
import gg.essential.universal.UDesktop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(RegionRenderCache.class)
public abstract class MixinRegionRenderCache {
    @Shadow protected abstract int getPositionIndex(BlockPos blockPos);

    @Shadow private IBlockState[] blockStates;

    @Shadow @Final private static IBlockState DEFAULT_STATE;

    @Shadow protected abstract IBlockState getBlockStateRaw(BlockPos pos);

    private static boolean shouldNotRunAgain = false;
    private static boolean firstFailure = false;

    /**
     * @author Wyvest
     * @reason Fix connected textures crash
     */
    @Overwrite
    public IBlockState getBlockState(BlockPos pos) {
        try {
            int i = this.getPositionIndex(pos);
            if (i < 0 || i >= this.blockStates.length) {
                if (!shouldNotRunAgain) {
                    try {
                        if (!CrashPatch.INSTANCE.getShouldNotShowFile().exists()) {
                            try {
                                CrashPatch.INSTANCE.getShouldNotShowFile().createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            EssentialAPI.getNotifications().push("CrashPatch", "CrashPatch just stopped your pack's Connected Textures from crashing your game!\n\nPlease tell the creators of your packs to add `connect=block` to their CTM files so we don't need to do hacky fixes like this!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    shouldNotRunAgain = true;
                }
                return DEFAULT_STATE;
            }
            IBlockState blockState = this.blockStates[i];
            if (blockState == null) {
                this.blockStates[i] = blockState = getBlockStateRaw(pos);
            }
            return blockState;
        } catch (Exception e) {
            if (!firstFailure) {
                firstFailure = true;
                Utils.pushNotification("CrashPatch", "CrashPatch's Connected Textures fix has failed!\nPlease contact inv.wtf/skyclient by click this notification!", 10f, () -> InternetUtils.browseURL(UDesktop.INSTANCE, "https://inv.wtf/skyclient"));
            }
            e.printStackTrace();
            return DEFAULT_STATE;
        }
    }
}
