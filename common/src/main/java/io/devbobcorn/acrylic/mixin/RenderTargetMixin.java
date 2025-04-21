package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
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
                GlStateManager._colorMask(false, false, false, true);

                var cmdEncoder = RenderSystem.getDevice().createCommandEncoder();

                if (cmdEncoder instanceof IGlCommandEncoder) {
                    // TODO: Find a better way?
                    ((IGlCommandEncoder) cmdEncoder).acrylic_mod$fillColorAlphaAndDepth(colorTexture, 0xFF000000, depthTexture, 1);
                }
            }
        }
    }
}
