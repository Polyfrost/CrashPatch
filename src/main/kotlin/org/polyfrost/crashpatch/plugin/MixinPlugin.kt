package org.polyfrost.crashpatch.plugin

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

import org.objectweb.asm.tree.ClassNode

class MixinPlugin : IMixinConfigPlugin {

    override fun getMixins(): MutableList<String> {
        val result = mutableListOf<String>()

        //#if MC<1.13
        //#if FORGE
        result.add("MixinGuiDupesFound")
        result.add("MixinTileEntityRendererDispatcher")
        //#endif
        result.add("MixinMinecraft")
        result.add("MixinWorldRenderer")
        //#else
        //$$ result.add("MixinEntryPointCatcher_UseCrashPatchGui")
        //$$ result.add("MixinInGameCatcher_UseCrashPatchGui")
        //$$ result.add("MixinMinecraft_CrashPatchInitUI")
        //$$ result.add("MixinMinecraft_CrashInitGui")
        //#if FABRIC && MC>=1.20.4
        //$$ result.add("MixinModLoaders_Debug")
        //#endif
        //#endif

        return result
    }

    override fun getRefMapperConfig(): String? = null
    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean = true

    override fun onLoad(mixinPackage: String) {
        // no-op
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
        // no-op
    }

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
        // no-op
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
        // no-op
    }

}