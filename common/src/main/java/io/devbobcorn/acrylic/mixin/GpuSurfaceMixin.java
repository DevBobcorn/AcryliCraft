package io.devbobcorn.acrylic.mixin;

import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import com.mojang.renderpearl.frontend.FrontendGpuSurface;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import io.devbobcorn.acrylic.client.rendering.AlphaFillRenderer;
import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;

// Minecraft presents the main render target through the swapchain-style GpuSurface:
// the main target's color view is handed to GpuSurface#blitFromTexture once per
// frame. We hook that call to force the framebuffer alpha opaque while in a level
// (so the window is not see-through over the rendered world), mirroring the old
// blitToScreen hack.
//
// In Minecraft 26.3 GpuSurface is an interface and the game uses the
// FrontendGpuSurface implementation, which is what this mixin targets.
@Mixin(FrontendGpuSurface.class)
public class GpuSurfaceMixin {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "blitFromTexture", at = @At("HEAD"))
    public void blitFromTexture(CommandEncoder commandEncoder, GpuTextureView textureView, CallbackInfo ci) {

        if (!AcrylicMod.getTransparencyEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        // Only fill alpha while in a level; in menus the window stays transparent
        // so the Fluent Design material remains visible behind it.
        if (minecraft.level == null || !(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            return;
        }

        RenderTarget mainTarget = minecraft.gameRenderer.mainRenderTarget();

        // Guard against any non-main-target present that might route through here.
        if (textureView != mainTarget.getColorTextureView()) {
            return;
        }

        if (!(commandEncoder instanceof FrontendCommandEncoder frontendEncoder)) {
            return;
        }

        var backend = frontendEncoder.backend();

        if (backend instanceof IGlCommandEncoder glBackend) {
            // OpenGL fast path: a masked glClear, kept for its lower per-frame overhead.
            GlStateManager._colorMask(8);
            glBackend.acrylic_mod$fillColorAlphaAndDepth(mainTarget.getColorTexture(), 0xFF000000, mainTarget.getDepthTexture(), 1);
        } else {
            // The experimental Vulkan backend (and any other non-OpenGL backend)
            // cannot use the color-mask clear trick, so fall back to a
            // backend-agnostic alpha-only fill performed via the RenderPearl RenderPipeline.
            AlphaFillRenderer.fillAlpha(textureView);
        }
    }
}
