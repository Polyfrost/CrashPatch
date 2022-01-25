package cc.woverflow.crashpatch.hooks;

public interface FontRendererHook {
    int drawCrashPatchSplitText(String str, int x, int y, int wrapWidth, int textColor);
}
