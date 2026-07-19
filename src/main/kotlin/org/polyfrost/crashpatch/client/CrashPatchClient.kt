package org.polyfrost.crashpatch.client

import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.utils.v1.dsl.createScreen
import org.polyfrost.oneconfig.utils.v1.dsl.mc

object CrashPatchClient {

    @JvmStatic
    var isCrashRequested = false

    @JvmStatic
    fun preInitialize() {
        CrashScanner.initialize()
    }

    fun initialize() {
        CrashPatchConfig.preload() // Initialize the config

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                CommandManager.literal("crashpatch")
                    .executes {
                        //? if < 26.2 {
                        /*mc.setScreen(CrashPatchConfig.createScreen())
                        *///? } else {
                        mc.gui.setScreen(CrashPatchConfig.createScreen())
                        //? }
                        Command.SINGLE_SUCCESS
                    }
                    .then(CommandManager.literal("reload").executes {
                        CrashScanner.submitCacheRequest()
                        val content = "Requested reload of crash data! Please wait." to CommonColors.GREEN
                        val (message, color) = content
                        val chatComponent = Component.literal("[${CrashPatchConstants.NAME}] $message").withColor(color)
                        //? if < 26.1 {
                        /*mc.gui.chat.addMessage(chatComponent)
                        *///? } elif < 26.2 {
                        /*mc.gui.chat.addClientSystemMessage(chatComponent)
                        *///? } else {
                        mc.gui.hud.chat.addClientSystemMessage(chatComponent)
                        //? }
                        Command.SINGLE_SUCCESS
                    })
                    .then(CommandManager.literal("crash").executes {
                        isCrashRequested = true
                        Command.SINGLE_SUCCESS
                    })

            )
        }
    }
}
