package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.common.ModContainer;

import java.util.Set;

public interface IPatchedCrashReport {
    Set<ModContainer> getSuspectedMods();
}
