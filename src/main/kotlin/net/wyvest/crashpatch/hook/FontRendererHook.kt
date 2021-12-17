package net.wyvest.crashpatch.hook

interface FontRendererHook {
    fun drawCrashPatchSplitText(str: String, x: Int, y: Int, wrapWidth: Int, textColor: Int): Int
}