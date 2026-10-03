package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.render.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface Mixin_AccessBufferBuilder {
    @Accessor("building")
    boolean isBuilding();
}
