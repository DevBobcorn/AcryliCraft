package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

import io.devbobcorn.acrylic.AcrylicMod;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {

    @Final
    @Shadow
    private LevelTargetBundle targets;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/Camera;FLcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    public void renderLevel_addSkyPass(LevelRenderer instance, FrameGraphBuilder frameGraphBuilder, Camera camera, float f, GpuBufferSlice gpuBufferSlice) {

        /*
        if (!AcrylicMod.getTransparencyEnabled()) {
            // Window transparency is not enabled, don't change vanilla behaviour
            return;
        }
        */

        FramePass framePass = frameGraphBuilder.addPass("clear2");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        framePass.executes(() -> {
            RenderTarget renderTarget = this.minecraft.getMainRenderTarget();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), ARGB.colorFromFloat(0.0F, 0.0F, 0.0F, 0.0F), renderTarget.getDepthTexture(), (double)1.0F);
        });

    }
}
