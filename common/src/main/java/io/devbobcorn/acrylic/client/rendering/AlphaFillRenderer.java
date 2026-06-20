package io.devbobcorn.acrylic.client.rendering;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.resources.Identifier;

/**
 * Backend-agnostic implementation of Acrylic's "fill the main render target
 * alpha channel with 1.0" hack.
 *
 * <p>The OpenGL path ({@code GlCommandEncoderMixin}) achieves this with a cheap
 * masked {@code glClear}, which is not expressible on Minecraft 26.2's
 * experimental Vulkan backend (Vulkan clears cannot mask individual channels).
 * This renderer instead drives the operation through the cross-backend Blaze3D
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

    private static RenderPipeline pipeline;
    private static GpuFormat pipelineFormat;
    private static GpuBuffer vertexBuffer;

    private AlphaFillRenderer() {
    }

    /**
     * Forces the alpha channel of the given color attachment fully opaque.
     */
    public static void fillAlpha(GpuTextureView colorView) {
        final GpuDevice device = RenderSystem.getDevice();
        final GpuFormat format = colorView.texture().getFormat();
        final RenderPipeline renderPipeline = getPipeline(format);
        final GpuBufferSlice vertices = getVertexBuffer(device).slice();

        try (RenderPass pass = device.createCommandEncoder().createRenderPass(
                () -> "Acrylic Alpha Fill", colorView, Optional.empty())) {
            pass.setPipeline(renderPipeline);
            pass.setVertexBuffer(0, vertices);
            pass.draw(0, FULLSCREEN_QUAD.length / 3, 0, 1);
        }
    }

    private static RenderPipeline getPipeline(GpuFormat format) {
        // The pipeline's color target format must match the attachment it is
        // used with, so rebuild it if the main render target format ever changes.
        if (pipeline == null || pipelineFormat != format) {
            pipeline = RenderPipeline.builder()
                    .withLocation(PIPELINE_LOCATION)
                    .withVertexShader(SHADER)
                    .withFragmentShader(SHADER)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withColorTargetState(new ColorTargetState(Optional.empty(), format, ColorTargetState.WRITE_ALPHA))
                    .build();
            pipelineFormat = format;
        }

        return pipeline;
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
