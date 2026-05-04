package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

import io.devbobcorn.acrylic.AcrylicMod;

@Mixin(value = RenderTarget.class, priority = 800)
public class RenderTargetMixin {

    @Shadow
    protected GpuTexture colorTexture;

    @Shadow
    protected GpuTexture depthTexture;

    @Inject(method = "blitToScreen()V", at = @At("HEAD"))
    public void blitToScreen(CallbackInfo ci) {

        if (!AcrylicMod.getTransparencyEnabled()) {
            // Window transparency is not enabled, don't change vanilla behaviour
            return;
        }

        /*
        if (colorTexture != Minecraft.getInstance().getMainRenderTarget().getColorTexture())
        {
            return;
        }
        */

        if (AcrylicMod.getFillMainRTAlpha()) { // For the final main RT blit, disableBlend is always true
            if ((Object) this == Minecraft.getInstance().getMainRenderTarget()) {
                GlStateManager._colorMask(8);

                var cmdEncoder = RenderSystem.getDevice().createCommandEncoder();

                if (cmdEncoder instanceof IGlCommandEncoder) {
                    // TODO: Find a better way?
                    ((IGlCommandEncoder) cmdEncoder).acrylic_mod$fillColorAlphaAndDepth(colorTexture, 0xFF000000, depthTexture, 1);
                }
            }
        }
    }
}
