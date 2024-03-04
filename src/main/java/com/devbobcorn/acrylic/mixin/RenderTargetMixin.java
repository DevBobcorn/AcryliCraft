package com.devbobcorn.acrylic.mixin;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

@Mixin(RenderTarget.class)
public class RenderTargetMixin {
    
    private Minecraft s_minecraft = null;

    @Redirect(
        method = "_blitToScreen(IIZ)V",
        at = @At(
            value = "INVOKE",
            ordinal = 0, // Match the 1st appearance only
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_colorMask(ZZZZ)V"
        )
    )
    private void GlStateManager_colorMaskRedirect(boolean r, boolean g, boolean b, boolean a) {
        
        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();
        }

        if (s_minecraft.level != null) {
            // In-game rendering, set alpha of the whole mainRT to 1
            var _this = (RenderTarget) (Object) this;

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, _this.frameBufferId);

            RenderSystem.colorMask(false, false, false, true);

            RenderSystem.clearColor(0, 0, 0, 1);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }

        // Enable alpha when blitting
        GlStateManager._colorMask(r, g, b, true);

    }
}
