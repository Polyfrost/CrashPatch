package vfyjxf.bettercrashes;


import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BetterCrashes.MODID, version = BetterCrashes.VERSION, name = BetterCrashes.NAME,dependencies = BetterCrashes.DEPENDENCIES)
public class BetterCrashes {
    public static final String MODID = "bettercrashes";
    public static final String NAME = "BetterCrashes";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:grimoire";
    public static final Logger logger = LogManager.getLogger("BetterCrashes");


}
