package vfyjxf.bettercrashes;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;

@Mod(modid = BetterCrashes.MODID, version = BetterCrashes.VERSION, name = BetterCrashes.NAME)
public class BetterCrashes {
    public static final String MODID = "bettercrashes";
    public static final String NAME = "BetterCrashes";
    public static final String VERSION = "@VERSION@";

    public static final Logger logger = LogManager.getLogger("BetterCrashes");
}
