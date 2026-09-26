package io.devbobcorn.acrylic.client.rendering;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.lwjgl.system.MemoryUtil;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.resources.Identifier;

/**
 * Backend-agnostic implementation of Acrylic's "fill the main render target
 * alpha channel with 1.0" hack.
 *
 * <p>The OpenGL path ({@code GlCommandEncoderMixin}) achieves this with a cheap
 * masked {@code glClear}, which is not expressible on Minecraft's experimental
 * Vulkan backend (Vulkan clears cannot mask individual channels). This renderer
 * instead drives the operation through the cross-backend RenderPearl
 * {@link RenderPipeline} abstraction: a full-screen draw with the color write
 * mask restricted to {@link ColorTargetState#WRITE_ALPHA}, so only the alpha
 * channel is overwritten while the rendered RGB scene is loaded and preserved.
 */
public final class AlphaFillRenderer {

    private static final Identifier SHADER = Identifier.fromNamespaceAndPath("acrylic", "core/alpha_fill");
    private static final Identifier PIPELINE_LOCATION = Identifier.fromNamespaceAndPath("acrylic", "pipeline/alpha_fill");

    // Two triangles covering the whole screen, expressed directly in NDC.
    private static final float[] FULLSCREEN_QUAD = {
            -1.0f, -1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
    };

    // Written once by the shader-reload thread (via RenderPipelinesMixin) and read
    // by the render thread, so the reference must be safely published.
    private static volatile RenderPipeline pipeline;
    private static GpuBuffer vertexBuffer;

    private AlphaFillRenderer() {
    }

    /**
     * The render pipeline used to fill the alpha channel.
     *
     * <p>Since Minecraft 26.3 the RenderPearl GPU abstraction requires pipelines
     * to be compiled up-front by {@code ShaderManager}, so this pipeline is
     * registered through {@code RenderPipelinesMixin} and looked up again with
     * {@link RenderSystem#getCompiledPipeline}. The main render target always
     * uses {@link GpuFormat#RGBA8_UNORM}, so a single pipeline instance can be
     * reused regardless of the active backend.
     */
    public static RenderPipeline getPipeline() {
        if (pipeline == null) {
            pipeline = RenderPipeline.builder()
                    .withLocation(PIPELINE_LOCATION)
                    .withVertexShader(SHADER)
                    .withFragmentShader(SHADER)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    // Disable face culling. The builder defaults culling to ON, and the
                    // full-screen triangles' winding is front-facing under OpenGL clip
                    // space but back-facing on the Vulkan backend (flipped clip-space Y),
                    // which would silently cull every fragment so the draw writes nothing.
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALPHA))
                    .build();
        }

        return pipeline;
    }

    /**
     * Forces the alpha channel of the given color attachment fully opaque.
     */
    public static void fillAlpha(GpuTextureView colorView) {
        final GpuDevice device = RenderSystem.getDevice();
        final GpuBufferSlice vertices = getVertexBuffer(device).slice();

        try (RenderPass pass = device.createCommandEncoder().createRenderPass(
                () -> "Acrylic Alpha Fill", colorView, Optional.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(getPipeline()));
            pass.setVertexBuffer(0, vertices);
            // RenderPass#draw maps straight onto vkCmdDraw, i.e.
            // draw(vertexCount, instanceCount, firstVertex, firstInstance).
            pass.draw(FULLSCREEN_QUAD.length / 3, 1, 0, 0);
        }
    }

    private static GpuBuffer getVertexBuffer(GpuDevice device) {
        if (vertexBuffer == null || vertexBuffer.isClosed()) {
            final ByteBuffer data = MemoryUtil.memAlloc(FULLSCREEN_QUAD.length * Float.BYTES);

            try {
                for (float value : FULLSCREEN_QUAD) {
                    data.putFloat(value);
                }

                data.flip();

                vertexBuffer = device.createBuffer(() -> "Acrylic Alpha Fill Vertices", GpuBuffer.USAGE_VERTEX, data);
            } finally {
                MemoryUtil.memFree(data);
            }
        }

        return vertexBuffer;
    }
}
