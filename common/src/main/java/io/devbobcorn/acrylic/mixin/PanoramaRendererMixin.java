package io.devbobcorn.acrylic.mixin;

import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.PanoramaRenderer;

@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(GuiGraphics guiGraphics, int i, int j, float f, float g, CallbackInfo callback) {
        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            // Don't render nothing
            callback.cancel();
        }
    }
}
