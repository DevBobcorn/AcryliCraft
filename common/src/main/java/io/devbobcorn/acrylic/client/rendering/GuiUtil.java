package io.devbobcorn.acrylic.client.rendering;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;

public class GuiUtil {

    @SuppressWarnings("null")
    public static void fillGradient(PoseStack poseStack, int x0, int y0, int x1, int y1, int z, int color0, int color1) {
        //RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(poseStack.last().pose(), bufferbuilder, x0, y0, x1, y1, z, color0, color1);
        bufferbuilder.build();
        RenderSystem.disableBlend();
        //RenderSystem.enableTexture();
    }

    @SuppressWarnings("null")
    public static void fillGradient(Matrix4f pose, BufferBuilder builder, int x0, int y0, int x1, int y1, int z, int color0, int color1) {
        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;
        builder.addVertex(pose, (float)x1, (float)y0, (float)z).setColor(r0, g0, b0, a0);
        builder.addVertex(pose, (float)x0, (float)y0, (float)z).setColor(r0, g0, b0, a0);
        builder.addVertex(pose, (float)x0, (float)y1, (float)z).setColor(r1, g1, b1, a1);
        builder.addVertex(pose, (float)x1, (float)y1, (float)z).setColor(r1, g1, b1, a1);
    }

    public static void blit(PoseStack poseStack, int x0, int y0, int z, float u0, float v0, int w, int h, int tex_w, int tex_h) {
        innerBlit(poseStack, x0, x0 + w, y0, y0 + h, z, w, h, u0, v0, tex_w, tex_h);
    }

    public static void blit(PoseStack poseStack, int x0, int y0, int w, int h, float u0, float v0, int w_, int h_, int tex_w, int tex_h) {
        innerBlit(poseStack, x0, x0 + w, y0, y0 + h, 0, w_, h_, u0, v0, tex_w, tex_h);
    }

    public static void blit(PoseStack poseStack, int x0, int y0, float u0, float v0, int w, int h, int tex_w, int tex_h) {
        blit(poseStack, x0, y0, w, h, u0, v0, w, h, tex_w, tex_h);
    }

    private static void innerBlit(PoseStack poseStack, int x0, int x1, int y0, int y1, int z, int w_, int h_, float u0, float v0, int uScale, int vScale) {
        innerBlit(poseStack.last().pose(), x0, x1, y0, y1, z, (u0 + 0.0F) / (float)uScale, (u0 + (float)w_) / (float)uScale, (v0 + 0.0F) / (float)vScale, (v0 + (float)h_) / (float)vScale);
    }

    @SuppressWarnings("null")
    private static void innerBlit(Matrix4f pose, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pose, (float)x0, (float)y1, (float)z).setUv(u0, v1);
        bufferbuilder.addVertex(pose, (float)x1, (float)y1, (float)z).setUv(u1, v1);
        bufferbuilder.addVertex(pose, (float)x1, (float)y0, (float)z).setUv(u1, v0);
        bufferbuilder.addVertex(pose, (float)x0, (float)y0, (float)z).setUv(u0, v0);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
