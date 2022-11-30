package cc.woverflow.crashpatch.gui

import cc.polyfrost.oneconfig.utils.color.ColorUtils

internal val GRAY_800 = ColorUtils.getColor(21, 22, 23, 255) // general background
internal val GRAY_700 = ColorUtils.getColor(34, 35, 38, 255) // log background
internal val GRAY_600 = ColorUtils.getColor(42, 44, 48, 255) // log header

internal val WHITE_90 = ColorUtils.getColor(255, 255, 255, 229) // text
internal val WHITE_80 = ColorUtils.getColor(255, 255, 255, 204) // subtext
internal val WHITE_60 = ColorUtils.getColor(255, 255, 255, 153) // logs

internal val BLUE_500 = ColorUtils.getColor(25, 103, 255, 255) // brand.primary
internal val BLUE_600 = ColorUtils.getColor(20, 82, 204, 255) // brand.hover

internal val BLACK_20 = ColorUtils.getColor(0, 0, 0, 12) // brand.hover

internal val TITLE = "Uh-oh. Your game crashed!"
internal val SUBTITLE_1 = "But, CrashPatch just saved the day! Feel free to ignore this, and"
internal val SUBTITLE_2 = "continue playing your game despite the crash."
internal val SUBTITLE_INIT_1 = "To fix this, "
internal val SUBTITLE_INIT_2 = "follow the tips listed below and / or "
internal val SUBTITLE_INIT_3 = "join the Discord server and make a support ticket."