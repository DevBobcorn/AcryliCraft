package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.systems.CommandEncoderBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.blaze3d.systems.CommandEncoder")
public interface CommandEncoderAccessor {

    @Accessor("backend")
    CommandEncoderBackend acrylic_mod$getBackend();
}
