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
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int j, CallbackInfo callback) {
        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            callback.cancel();

            int rgb = (int) AcrylicConfig.getInstance().getValue(AcrylicConfig.BACKGROUND_COLOR_RGB);
            int alpha = (int) AcrylicConfig.getInstance().getValue(AcrylicConfig.BACKGROUND_COLOR_ALPHA);
            if (alpha != 0) {
                int bgColor = (alpha << 24) | (rgb & 0x00FFFFFF);
                guiGraphicsExtractor.fill(0, 0, i, j, bgColor);
            }
        }
    }
}
