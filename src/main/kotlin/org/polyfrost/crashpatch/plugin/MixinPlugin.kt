package org.polyfrost.crashpatch.plugin

import com.bawnorton.mixinsquared.MixinSquaredBootstrap
import com.llamalad7.mixinextras.MixinExtrasBootstrap
import org.polyfrost.crashpatch.client.crashes.LogScanner
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

import org.objectweb.asm.tree.ClassNode

class MixinPlugin : IMixinConfigPlugin {

    override fun getMixins(): MutableList<String> {
        val result = mutableListOf<String>()

        result.add("Mixin_EntryPointErrorForceCrashPatchUI")
        result.add("Mixin_InGameCatcherForceCrashPatchUI")
        result.add("Mixin_InGameCatcherKeepWorld")
        result.add("Mixin_LogCrashWhenScreenSkipped")
        //? if <26.1 {
        /*result.add("Mixin_RelayWorkerThreadCrash")
        *///? }
        result.add("Mixin_CrashPatchInitUI")
        result.add("Mixin_CrashInitGui")
        result.add("Mixin_ModLoaders_Debug")

        return result
    }

    override fun getRefMapperConfig(): String? = null
    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean = true

    override fun onLoad(mixinPackage: String) {
        LogScanner.install()
        MixinExtrasBootstrap.init()
        MixinSquaredBootstrap.init()
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

}