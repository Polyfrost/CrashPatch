package vfyjxf.bettercrashes;

import io.github.crucible.grimoire.common.api.grimmix.Grimmix;
import io.github.crucible.grimoire.common.api.grimmix.GrimmixController;
import io.github.crucible.grimoire.common.api.grimmix.lifecycle.IConfigBuildingEvent;

@Grimmix(id = "bettercrashesgrimmix", name = "Better Crashes Grimmix")
public class BetterCrashesGrimmix extends GrimmixController {

    @Override
    public void buildMixinConfigs(IConfigBuildingEvent event) {
        event.createBuilder("bettercrashes/mixins.bettercrashes.json")
             .mixinPackage("vfyjxf.bettercrashes.mixins")
             .clientMixins("client.MixinMinecraft")
             .commonMixins("MixinCrashReport")
             .refmap("@MIXIN_REFMAP@")
             .verbose(true)
             .required(true)
             .build();
    }


}
