package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected void renderPanorama(GuiGraphics guiGraphics, float f) { }

    @Shadow
    protected void renderBlurredBackground(float f) { }

    @Shadow
    protected void renderMenuBackground(GuiGraphics guiGraphics) { }

    @Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo callback) {

        if (!(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            if (minecraft.level == null)  {
                // Render panorama
                renderPanorama(guiGraphics, f);
            }
            renderBlurredBackground(f);
            renderMenuBackground(guiGraphics);
        } else { // Transparent enabled
            if (minecraft.level != null) { // Rendering a client world
                // Render blurred background
                renderBlurredBackground(f);
                renderMenuBackground(guiGraphics);
            } else { // Not rendering a client world
                // Vanilla will render the panorama to hide the pixels beneath,
                // but we don't use panorama here so clear them up.
                RenderSystem.clearColor(0, 0, 0, 0);
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            }
        }

        callback.cancel();
    }
}
