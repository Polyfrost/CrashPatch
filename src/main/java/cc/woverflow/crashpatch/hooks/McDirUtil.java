package cc.woverflow.crashpatch.hooks;

import java.io.File;

public class McDirUtil {
    public static File getMcDir() {
        return new File(System.getProperty("user.dir"));
    }
}
