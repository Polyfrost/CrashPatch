package org.polyfrost.crashpatch

import net.minecraft.util.ChatComponentText
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command
import org.polyfrost.universal.ChatColor
import org.polyfrost.universal.UMinecraft
import org.polyfrost.utils.v1.dsl.openUI

@Command(CrashPatch.ID)
object CrashPatchCommand {

    @Command
    fun main() {
        CrashPatchConfig.openUI()
    }

    @Command
    fun reload() {
        if (CrashScanStorage.downloadJson()) {
            CrashScanStorage.simpleCache.clear()
            UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Successfully reloaded JSON file!"))
        } else {
            UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Failed to reload the JSON file!"))
        }
    }

    @Command
    fun crash() {
        CrashPatch.requestedCrash = true
    }

}
