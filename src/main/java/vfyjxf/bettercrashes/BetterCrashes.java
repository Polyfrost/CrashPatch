package vfyjxf.bettercrashes;


import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;

import java.io.File;

@Mod(modid = BetterCrashes.MODID, version = BetterCrashes.VERSION, name = BetterCrashes.NAME,dependencies = BetterCrashes.DEPENDENCIES)
public class BetterCrashes {
    public static final String MODID = "bettercrashes";
    public static final String NAME = "BetterCrashes";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:grimoire";
    public static final Logger logger = LogManager.getLogger("BetterCrashes");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        BetterCrashesConfig.loadConfig(new File(Minecraft.getMinecraft().mcDataDir,"config/BetterCrashes.cfg"));
    }
}
