package io.devbobcorn.acrylic.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import io.devbobcorn.acrylic.client.rendering.AlphaFillRenderer;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Registers Acrylic's custom alpha-fill pipeline with the vanilla pipeline list.
 *
 * <p>Minecraft 26.3's RenderPearl abstraction compiles every pipeline up-front in
 * {@code ShaderManager} and no longer accepts an uncompiled {@link RenderPipeline}
 * when setting a render pass. Adding the pipeline to {@code requiredPipelines()}
 * makes it go through the standard compilation pass and become available through
 * {@code RenderSystem#getCompiledPipeline}.
 */
@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {

    @Inject(method = "requiredPipelines", at = @At("RETURN"), cancellable = true)
    private static void acrylic_mod$addAlphaFillPipeline(CallbackInfoReturnable<List<RenderPipeline>> cir) {
        var pipelines = new ArrayList<>(cir.getReturnValue());
        pipelines.add(AlphaFillRenderer.getPipeline());
        cir.setReturnValue(pipelines);
    }
}
