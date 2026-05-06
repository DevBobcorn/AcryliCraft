package io.devbobcorn.acrylic.mixin;

import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Shadow
    protected Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "extractBackground", cancellable = true)
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int i, int j, float f, CallbackInfo callback) {

        var conf = AcrylicConfig.getInstance();

        if ((boolean) conf.getValue(AcrylicConfig.TRANSPARENT_WINDOW) && (boolean) conf.getValue(AcrylicConfig.REMOVE_SCREEN_BACKGROUND)) {
            if (minecraft.level == null) {
                callback.cancel();

                // Cancelling background rendering will also cancel panorama rendering, which includes our custom background color.
                // So we have to render the background color here as well.
                int rgb = (int) AcrylicConfig.getInstance().getValue(AcrylicConfig.BACKGROUND_COLOR_RGB);
                int alpha = (int) AcrylicConfig.getInstance().getValue(AcrylicConfig.BACKGROUND_COLOR_ALPHA);
                Screen screen = (Screen) (Object) this;
                if (alpha != 0) {
                    int bgColor = (alpha << 24) | (rgb & 0x00FFFFFF);
                    guiGraphicsExtractor.fill(0, 0, screen.width, screen.height, bgColor);
                }
            }
        }
    }
}
