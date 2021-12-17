/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/IPatchedCrashReport.java
 *The source file uses the MIT License.
 */

package net.wyvest.bettercrashes.utils;

import net.minecraftforge.fml.common.ModContainer;

import java.util.Set;

/**
 * @author Runemoro
 */
public interface IPatchedCrashReport {
    Set<ModContainer> getSuspectedMods();
}
