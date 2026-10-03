package org.polyfrost.crashpatch.test

//? if > 1.8.9
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
//? if = 1.8.9 {
/*import net.fabricmc.loader.api.FabricLoader
import net.ornithemc.osl.entrypoints.api.ModInitializer
*///?}
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.MixinEnvironment.Option
import org.spongepowered.asm.mixin.transformer.IMixinTransformer

/**
 * Audits mixins to ensure their validity without launching a full Minecraft client.
 * Implementation inspired by [Skyblocker](https://github.com/SkyblockerMod/Skyblocker).
 */
class MixinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupEnvironment() {
            //? if > 1.8.9 {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            //?} else {
            /*FabricLoader.getInstance().invokeEntrypoints(
                ModInitializer.ENTRYPOINT_KEY,
                ModInitializer::class.java,
                ModInitializer::init,
            )
            Bootstrap.init()
            *///?}
        }
    }

    @Test
    fun `mixins load successfully`() {
        val environment = MixinEnvironment.getCurrentEnvironment()
        Assertions.assertInstanceOf(
            IMixinTransformer::class.java,
            environment.activeTransformer,
        )
        // Fabric Loader enables refmap remapping in development, which makes Mixin retry failed
        // target selection with the method descriptor stripped. Production launches do no such
        // thing, so disable it to match their strictness — otherwise a selector whose descriptor
        // does not exist on this Minecraft version still resolves by name alone and the audit
        // passes on a mixin that cannot apply in production.
        environment.setOption(Option.REFMAP_REMAP, false)
        environment.audit()
    }
}
