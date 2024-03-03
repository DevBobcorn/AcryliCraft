package com.devbobcorn.sky_painter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;

@Mixin(RenderTarget.class)
public class RenderTargetMixin {
    
    @Redirect(
        method = "_blitToScreen(IIZ)V",
        at = @At(
            value = "INVOKE",
            ordinal = 0, // Match the 1st appearance only
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_colorMask(ZZZZ)V"
        )
    )
    private void GlStateManager_colorMaskRedirect(boolean r, boolean g, boolean b, boolean a) {
        GlStateManager._colorMask(true, true, true, true);
    }
}
