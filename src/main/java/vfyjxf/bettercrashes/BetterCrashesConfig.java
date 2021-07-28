package vfyjxf.bettercrashes;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class BetterCrashesConfig {

    public static boolean  enableUploadCrash = false;

    public static void loadConfig(File configFile){
        Configuration config = new Configuration(configFile);
        config.load();
        enableUploadCrash = config.get("upload","enableUploadCrash",false,
                "if true,we will use \"https://paste.ubuntu.com\" to upload crash report.").getBoolean();
        config.save();
    }

}
