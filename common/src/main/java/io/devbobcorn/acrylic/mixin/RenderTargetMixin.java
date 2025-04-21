package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

import io.devbobcorn.acrylic.AcrylicMod;

// Set priority to 800 to make sure this injection is called before
// the one in Sodium's RenderTargetMixin does, their mixin then does
// an optimized screen blit and cancels blitToScreen call.
// https://github.com/CaffeineMC/sodium-fabric/blob/dev/common/src/main/java/net/caffeinemc/mods/sodium/mixin/features/render/compositing/RenderTargetMixin.java
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

        if (AcrylicMod.getFillMainRTAlpha()) { // For the final main RT blit, disableBlend is always true
            if ((Object) this == Minecraft.getInstance().getMainRenderTarget()) {
                /*
                // Fill alpha channel for main render target
                GL32C.glBindFramebuffer(GL32C.GL_DRAW_FRAMEBUFFER, this.frameBufferId);
                // Use GLStateManager to make sure cached current color mask is updated
                GlStateManager._colorMask(false, false, false, true);
                GL11.glClearColor(0, 0, 0, 1);
                GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GL32C.glBindFramebuffer(GL32C.GL_DRAW_FRAMEBUFFER, 0);

                // Reset GL Clear color in case Sodium replaces the blit procedure
                GlStateManager._colorMask(true, true, true, true);
                 */
            }
        }
    }

    /*
    // This redirect will be bypassed if Sodium's RenderTargetMixin
    // is applied, in which case we don't need to tweak color mask
    @Redirect(
            method = "_blitToScreen(IIZ)V",
            at = @At(
                    value = "INVOKE",
                    ordinal = 0, // Match the 1st appearance only
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_colorMask(ZZZZ)V"
            )
    )
    private void GlStateManager_colorMaskRedirect(boolean r, boolean g, boolean b, boolean a) {

        if (!AcrylicMod.getTransparencyEnabled()) {
            // Window transparency is not enabled, don't change vanilla behaviour
            GlStateManager._colorMask(r, g, b, a);
            return;
        }

        if ((Object) this == Minecraft.getInstance().getMainRenderTarget()) {
            // Enable alpha when blitting
            GlStateManager._colorMask(r, g, b, true);
        } else {
            // Not main render target, don't change vanilla behaviour
            GlStateManager._colorMask(r, g, b, a);
        }
    }*/
}
