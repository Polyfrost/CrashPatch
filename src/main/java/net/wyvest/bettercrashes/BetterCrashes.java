package net.wyvest.bettercrashes;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BetterCrashes.MODID, version = BetterCrashes.VERSION, name = BetterCrashes.NAME)
public class BetterCrashes {
    public static final String MODID = "bettercrashes";
    public static final String NAME = "BetterCrashes";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("BetterCrashes");

}
