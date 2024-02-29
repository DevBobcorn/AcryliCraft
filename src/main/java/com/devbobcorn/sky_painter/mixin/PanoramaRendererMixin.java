package com.devbobcorn.sky_painter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.PanoramaRenderer;

@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {

    @Inject(at = @At("HEAD"), method = "render(FF)V", cancellable = true)
    public void render(float timeDelta, float opacity, CallbackInfo callback) {
        // Don't render nothing
        callback.cancel();
    }
}
