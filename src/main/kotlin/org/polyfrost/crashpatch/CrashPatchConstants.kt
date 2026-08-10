package org.polyfrost.crashpatch

import net.fabricmc.loader.api.FabricLoader

object CrashPatchConstants {

    // Placeholders replaced at build time from gradle.properties by the bloom DGT plugin
    const val ID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"

    val gameDirectory by lazy { FabricLoader.getInstance().gameDir }

    @JvmField
    var recoveredFromCrash = false

}