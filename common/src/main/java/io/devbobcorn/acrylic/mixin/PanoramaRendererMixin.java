package io.devbobcorn.acrylic.mixin;

import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.Panorama;

@Mixin(Panorama.class)
public class PanoramaRendererMixin {

    @Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int j, boolean bl, CallbackInfo callback) {
        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            callback.cancel();
        }
    }
}
