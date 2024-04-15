package io.devbobcorn.acrylic.mixin;

import io.devbobcorn.acrylic.AcrylicMod;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.client.window.IWindow;
import io.devbobcorn.acrylic.client.window.WindowUtil;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    //private static final ResourceLocation M_TEX = new ResourceLocation("textures/gui/title/edition.png");

    @Unique
    private Minecraft s_minecraft = null;

    @Unique
    @SuppressWarnings("null")
    public void acrylic_mod$renderString(GuiGraphics guiGraphics, String str, int x, int y) {
        guiGraphics.drawString(s_minecraft.font, str, x, y, 16777215);
    }

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    public void renderHead(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            // Clear whatever that has been rendered in the background
            RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
        }

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();

        } else {

            if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.SHOW_DEBUG_INFO)) {
                // Draw debug info
                var windowHandle = AcrylicMod.getWindowHandle();

                acrylic_mod$renderString(guiGraphics, "Window Handle: " + String.format("0x%016X", windowHandle) +
                        " (Transparency Enabled: " + AcrylicMod.getTransparencyEnabled() + ")", 2, 2);
                acrylic_mod$renderString(guiGraphics, GlUtil.getRenderer() + ", OpenGL " + GlUtil.getOpenGLVersion(), 2, 12);
            }
        }

        /*
        var _this = (TitleScreen) (Object) this;

        int halfWidth  = _this.width  >> 1;
        int halfHeight = _this.height >> 1;

        GuiUtil.fillGradient(poseStack,      0,       0,   halfWidth,   halfHeight, 400, (int) 0xFF000000, (int) 0xFF000000); // L-Upper, Black Opaque
        GuiUtil.fillGradient(poseStack, halfWidth,       0, _this.width,   halfHeight, 400, (int) 0xFFFFFFFF, (int) 0xFFFFFFFF); // R-Upper, White Opaque
        GuiUtil.fillGradient(poseStack,      0, halfHeight,   halfWidth, _this.height, 400, (int) 0x80000000, (int) 0x80000000); // L-Lower, Black Transparent
        GuiUtil.fillGradient(poseStack, halfWidth, halfHeight, _this.width, _this.height, 400, (int) 0x80FFFFFF, (int) 0x80FFFFFF); // R-Lower, White Transparent
        */

        //renderTex(poseStack, M_TEX, mouseX, mouseY, 98, 14, 128, 16);
    }

}
