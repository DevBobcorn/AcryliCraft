package io.devbobcorn.acrylic.mixin.fabric;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class)
public class LevelRendererFabricMixin {

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
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    public void renderLevel_addSkyPass(LevelRenderer instance, FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState, GpuBufferSlice gpuBufferSlice) {

        FramePass framePass = frameGraphBuilder.addPass("clear2");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        framePass.executes(() -> {
            RenderTarget renderTarget = this.targets.main.get();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), ARGB.colorFromFloat(0.0F, 0.0F, 0.0F, 0.0F), renderTarget.getDepthTexture(), (double)1.0F);
        });

    }
}
