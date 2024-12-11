package org.polyfrost.crashpatch

import net.minecraft.util.ChatComponentText
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command
import org.polyfrost.oneconfig.utils.v1.dsl.openUI
import org.polyfrost.universal.ChatColor
import org.polyfrost.universal.UMinecraft

@Command(CrashPatch.ID)
object CrashPatchCommand {

    @Command
    fun main() {
        CrashPatchConfig.openUI()
    }

    @Command
    fun reload() {
        if (CrashScanStorage.downloadJson()) {
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
