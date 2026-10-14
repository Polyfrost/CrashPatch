package org.polyfrost.crashpatch.mixin;

//? if > 1.8.9 {
import fudge.notenoughcrashes.config.NecConfig;
import fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher;
import fudge.notenoughcrashes.mixinhandlers.InGameCatcher;
import fudge.notenoughcrashes.stacktrace.CrashUtils;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 26.3
import java.nio.file.Path;

@Mixin(value = InGameCatcher.class, remap = false)
public class Mixin_LogCrashWhenScreenSkipped {
    //~ if >= 26.3 '"displayCrashScreen"' -> '"displayCrashScreen(Lnet/minecraft/CrashReport;IZLjava/nio/file/Path;)V"'
    @Inject(method = "displayCrashScreen(Lnet/minecraft/CrashReport;IZLjava/nio/file/Path;)V", at = @At("HEAD"))
    //~ if >= 26.3 'boolean clientCrash, CallbackInfo' -> 'boolean clientCrash, Path world, CallbackInfo'
    private static void crashpatch$reportBeforeBailing(CrashReport report, int crashCount, boolean clientCrash, Path world, CallbackInfo ci) {
        if (EntryPointCatcher.crashedDuringStartup() || crashCount > NecConfig.getCurrent().crashLimit()) {
            CrashUtils.outputReport(report, clientCrash);
        }
    }
}
//?}
