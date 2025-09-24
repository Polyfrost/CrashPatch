package org.polyfrost.crashpatch.plugin

import com.bawnorton.mixinsquared.MixinSquaredBootstrap
import com.llamalad7.mixinextras.MixinExtrasBootstrap
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class EarlyMixinPlugin : IMixinConfigPlugin {
    override fun onLoad(p0: String?) {
        MixinExtrasBootstrap.init()
        MixinSquaredBootstrap.init()
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(p0: String?, p1: String?): Boolean {
        return true
    }

    override fun acceptTargets(
        p0: Set<String?>?,
        p1: Set<String?>?
    ) {
    }

    override fun getMixins(): List<String?>? {
        return null
    }

    override fun preApply(
        p0: String?,
        p1: ClassNode?,
        p2: String?,
        p3: IMixinInfo?
    ) {
    }

    override fun postApply(
        p0: String?,
        p1: ClassNode?,
        p2: String?,
        p3: IMixinInfo?
    ) {
    }
}