package cc.woverflow.crashpatch.utils

import cc.woverflow.crashpatch.CrashPatch.isPatcher
import cc.woverflow.crashpatch.hooks.EnhancementManagerHook
import cc.woverflow.crashpatch.hooks.MinecraftHook
import cc.woverflow.crashpatch.hooks.SimpleReloadableResourceManagerHook
import club.sk1er.patcher.util.enhancement.EnhancementManager
import club.sk1er.patcher.util.enhancement.ReloadListener
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IResourceManagerReloadListener

object ModCompatUtils {
    fun resetPatcherFontRenderer() {
        if (isPatcher) {
            ((Minecraft.getMinecraft() as MinecraftHook).resourceManager as SimpleReloadableResourceManagerHook).removeIf { a: IResourceManagerReloadListener? -> a is ReloadListener }
            (Minecraft.getMinecraft() as MinecraftHook).resourceManager.registerReloadListener(ReloadListener())
        }
    }

    fun onPatcherTick() {
        /*/
        if (isPatcher && !(EnhancementManager.getInstance() as EnhancementManagerHook).ticking) {
            EnhancementManager.getInstance().tick()
        }

         */
    }
}