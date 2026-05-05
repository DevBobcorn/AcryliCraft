package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;

// Set priority to 800 to make sure this injection is called before
// the one in Sodium's RenderTargetMixin does, their mixin then does
// an optimized screen blit and cancels blitToScreen call.
// https://github.com/CaffeineMC/sodium-fabric/blob/dev/common/src/main/java/net/caffeinemc/mods/sodium/mixin/features/render/compositing/RenderTargetMixin.java
@Mixin(value = RenderTarget.class, priority = 800)
public class RenderTargetMixin {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    protected GpuTexture colorTexture;

    @Shadow
    protected GpuTexture depthTexture;

    @Unique
    private static Minecraft s_minecraft;

    @Unique
    private static AcrylicConfig s_config;

    @Unique
    private static boolean s_fillAlpha;

    @Unique
    private static Object s_level;

    @Unique
    private static boolean s_transparentWindow;

    @Inject(method = "blitToScreen", at = @At("HEAD"))
    public void blitToScreen(CallbackInfo ci) {

        if (!AcrylicMod.getTransparencyEnabled()) {
            return;
        }

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();
            s_config = AcrylicConfig.getInstance();
        }

        Object level = s_minecraft.level;
        boolean transparentWindow = (boolean) s_config.getValue(AcrylicConfig.TRANSPARENT_WINDOW);

        if (level != s_level || transparentWindow != s_transparentWindow) {
            s_level = level;
            s_transparentWindow = transparentWindow;
            s_fillAlpha = level != null && transparentWindow;
        }

        if (s_fillAlpha) {
            if ((Object) this == s_minecraft.getMainRenderTarget()) {
                GlStateManager._colorMask(8);

                var cmdEncoder = RenderSystem.getDevice().createCommandEncoder();
                var backend = ((CommandEncoderAccessor) cmdEncoder).acrylic_mod$getBackend();

                if (backend instanceof IGlCommandEncoder) {
                    ((IGlCommandEncoder) backend).acrylic_mod$fillColorAlphaAndDepth(colorTexture, 0xFF000000, depthTexture, 1);
                } else {
                    LOGGER.warn("[Acrylic] CommandEncoder backend is not an IGlCommandEncoder: {}",
                            backend == null ? "null" : backend.getClass().getName());
                }
            }
        }
    }
}
