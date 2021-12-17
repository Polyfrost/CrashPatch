package net.wyvest.crashpatch.hook

import net.minecraftforge.fml.common.ModContainer

interface CrashReportHook {
    val suspectedMods: Set<ModContainer>?
}