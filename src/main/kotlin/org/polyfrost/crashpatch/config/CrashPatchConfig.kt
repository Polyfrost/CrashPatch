package org.polyfrost.crashpatch.config

import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.annotations.*
import org.polyfrost.universal.UDesktop.browse
import java.net.URI


object CrashPatchConfig : Config("crashpatch.json", "/assets/crashpatch/crashpatch_dark.svg", "CrashPatch", Category.QOL) {

    // Toggles
    @Switch(
        title = "Catch crashes during gameplay",
        description = "Catch crashes whilst in-game, and prevent the game from closing",
        subcategory = "Patches"
    )
    var inGameCrashPatch = true

    @Switch(
        title = "Patch crashes during launch",
        description = "Catch crashes during initialization, & display a message.",
        subcategory = "Patches"
    )
    var initCrashPatch = true

    @Switch(
        title = "Display disconnection causes",
        description = "Display a message when a reason is found for a disconnect.",
        subcategory = "Patches"
    )
    var disconnectCrashPatch = true

    // Limits
    //@Info( // todo
    //    text = "It's recommended to leave the world after a few crashes, and outright quit the game if there are more; this is to avoid severe instability",
    //    type = InfoType.WARNING,
    //    size = 2,
    //    subcategory = "Limits"
    //)
    //var ignored: Boolean = false

    @Slider(
        title = "World Leave Limit",
        min = 1f,
        max = 20f,
        step = 1F,
        subcategory = "Limits"
    )
    var leaveLimit = 3

    @Slider(
        title = "Crash Limit",
        min = 1f,
        max = 20f,
        step = 1F,
        subcategory = "Limits"
    )
    var crashLimit = 5

    @Switch(
        title = "Deobfuscate Crash Log",
        description = "Makes certain class names more readable through deobfuscation",
        subcategory = "Logs"
    )
    var deobfuscateCrashLog = true

    @Dropdown(
        title = "Log uploader",
        description = "The method used to upload the crash log.",
        options = ["hst.sh", "mclo.gs (Aternos)"],
        subcategory = "Logs"
    )
    var crashLogUploadMethod = 0

    @Button(
        title = "Polyfrost support",
        text = "Discord"
    )
    var supportDiscord = Runnable { browse(URI.create("https://polyfrost.cc/discord/")) }

    @Button(
        title = "Crash game",
        text = "Crash"
    )
    var crashGame = Runnable { throw Throwable("java.lang.NoClassDefFoundError: xyz/matthewtgm/requisite/keybinds/KeyBind at lumien.custommainmenu.configuration.ConfigurationLoader.load(ConfigurationLoader.java:142) club.sk1er.bossbarcustomizer.BossbarMod.loadConfig cc.woverflow.hytils.handlers.chat.modules.modifiers.DefaultChatRestyler Failed to login: null The Hypixel Alpha server is currently closed! net.kdt.pojavlaunch macromodmodules") }
}