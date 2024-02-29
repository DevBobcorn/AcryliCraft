package com.devbobcorn.sky_painter.client;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ICloudRenderHandler;

public class MicaCloudRender implements ICloudRenderHandler {
    private boolean generateClouds = true;
    @Nullable
    private VertexBuffer cloudBuffer;

    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    @Nullable
    private CloudStatus prevCloudsType;

    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");

    private static final Vec3 CLOUD_COLOR = new Vec3(0f, 0f, 0f);

    public void render(int ticks, float partialTick, PoseStack poseStack, ClientLevel level, Minecraft minecraft,
            double camX, double camY, double camZ) {
        
        float cloudHeight = level.effects().getCloudHeight();

        if (!Float.isNaN(cloudHeight)) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,       GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.depthMask(true);
            float sizeHor = 12.0F;
            float sizeVer = 4.0F;

            double offsetX = (double) (((float) ticks + partialTick) * 0.03F);
            double posX = (camX + offsetX) / sizeHor;
            double posY = (double) (cloudHeight - (float) camY + 0.33F);
            double posZ = camZ / 12.0D + (double) 0.33F;
            posX -= (double) (Mth.floor(posX / 2048.0D) * 2048);
            posZ -= (double) (Mth.floor(posZ / 2048.0D) * 2048);
            float decimalX = (float) (posX - (double) Mth.floor(posX));
            float decimalY = (float) (posY / sizeVer - (double) Mth.floor(posY / sizeVer)) * sizeVer;
            float decimalZ = (float) (posZ - (double) Mth.floor(posZ));
            Vec3 color = CLOUD_COLOR; // level.getCloudColor(partialTick);
            //Vec3 color = Vec3.ZERO;
            int intX = (int) Math.floor(posX);
            int intY = (int) Math.floor(posY / 4.0D);
            int intZ = (int) Math.floor(posZ);
            if (intX != this.prevCloudX || intY != this.prevCloudY || intZ != this.prevCloudZ
                    || minecraft.options.getCloudsType() != this.prevCloudsType
                    || this.prevCloudColor.distanceToSqr(color) > 2.0E-4D) {
                this.prevCloudX = intX;
                this.prevCloudY = intY;
                this.prevCloudZ = intZ;
                this.prevCloudColor = color;
                this.prevCloudsType = minecraft.options.getCloudsType();
                this.generateClouds = true;
            }

            if (this.generateClouds) {
                this.generateClouds = false;
                BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.close();
                }

                this.cloudBuffer = new VertexBuffer();
                this.buildClouds(bufferbuilder, posX, posY, posZ, color);
                bufferbuilder.end();
                this.cloudBuffer.upload(bufferbuilder);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            // Customize fog
            FogRenderer.levelFogColor();
            RenderSystem.setShaderFogColor(0.0F, 0.0F, 0.0F, 0.0F);

            poseStack.pushPose();
            poseStack.scale(12.0F, 1.0F, 12.0F);
            poseStack.translate((double)(-decimalX), (double)decimalY, (double)(-decimalZ));
            if (this.cloudBuffer != null) {
                // Get projection matrix - See GameRenderer.class, line 1023
                PoseStack projectionStack = new PoseStack();
                // TODO: double d0 = minecraft.gameRenderer.getFov(minecraft.gameRenderer.getMainCamera(), partialTick, true);
                double d0 = minecraft.options.fov;
                projectionStack.last().pose().multiply(minecraft.gameRenderer.getProjectionMatrix(d0));
                this.bobHurt(projectionStack, partialTick);
                if (minecraft.options.bobView) {
                    this.bobView(projectionStack, partialTick);
                }

                Matrix4f projectionMatrix = projectionStack.last().pose();
                // END Get projection matrix

               int i1 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;
   
               for(int l = i1; l < 2; ++l) {
                  if (l == 0) {
                     RenderSystem.colorMask(false, false, false, false);
                  } else {
                     RenderSystem.colorMask(true, true, true, true);
                  }
   
                  ShaderInstance shaderinstance = RenderSystem.getShader();
                  this.cloudBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);
               }
            }
   
            poseStack.popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private void buildClouds(BufferBuilder buffer, double posX, double posY, double posZ, Vec3 color) {
        int range = 4; // Default: 4

        float scale = 1.0F;

        float dist = 1.0F / scale; // Default: 1.0F, should be 1/8 of size
        float size = 8.0F / scale; // 8.0F

        float tkns = 4.0F; // Thickness
        float uvpx = 0.00390625F * scale; // i.e. 1/256
        float num = 9.765625E-4F;
        float uvOrgX = (float) Mth.floor(posX) * uvpx;
        float uvOrgZ = (float) Mth.floor(posZ) * uvpx;
        float colR = (float) color.x;
        float colG = (float) color.y;
        float colB = (float) color.z;
        float r09 = colR * 0.9F;
        float g09 = colG * 0.9F;
        float b09 = colB * 0.9F;
        float r07 = colR * 0.7F;
        float g07 = colG * 0.7F;
        float b07 = colB * 0.7F;
        float r08 = colR * 0.8F;
        float g08 = colG * 0.8F;
        float b08 = colB * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float offsetY = (float) Math.floor(posY / 4.0D) * tkns;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for (int blocX = -range + 1; blocX <= range; ++blocX) {
                for (int blocZ = -range + 1; blocZ <= range; ++blocZ) {
                    float offsetX = (float) (blocX * size);
                    float offsetZ = (float) (blocZ * size);
                    if (offsetY > -5.0F) {
                        buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + size))
                            .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                            .color(r07, g07, b07, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + size), (double) (offsetY + 0.0F), (double) (offsetZ + size))
                            .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                            .color(r07, g07, b07, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + size), (double) (offsetY + 0.0F), (double) (offsetZ + 0.0F))
                            .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                            .color(r07, g07, b07, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + 0.0F))
                            .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                            .color(r07, g07, b07, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (offsetY <= 5.0F) {
                        buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + tkns - num), (double) (offsetZ + size))
                            .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                            .color(colR, colG, colB, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + size), (double) (offsetY + tkns - num), (double) (offsetZ + size))
                            .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                            .color(colR, colG, colB, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + size), (double) (offsetY + tkns - num), (double) (offsetZ + 0.0F))
                            .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                            .color(colR, colG, colB, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + tkns - num), (double) (offsetZ + 0.0F))
                            .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                            .color(colR, colG, colB, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (blocX > -1) { // Facing -X
                        for (int i1 = 0; i1 < 8; ++i1) {
                            buffer.vertex((double) (offsetX + i1 * dist + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + size))
                                .uv((offsetX + i1 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + i1 * dist + 0.0F), (double) (offsetY + tkns), (double) (offsetZ + size))
                                .uv((offsetX + i1 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + i1 * dist + 0.0F), (double) (offsetY + tkns), (double) (offsetZ + 0.0F))
                                .uv((offsetX + i1 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + i1 * dist + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + 0.0F))
                                .uv((offsetX + i1 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (blocX <= 1) { // Facing +X
                        for (int j2 = 0; j2 < 8; ++j2) {
                            buffer.vertex((double) (offsetX + j2 * dist + dist - num), (double) (offsetY + 0.0F), (double) (offsetZ + size))
                                .uv((offsetX + j2 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + j2 * dist + dist - num), (double) (offsetY + tkns), (double) (offsetZ + size))
                                .uv((offsetX + j2 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + size) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + j2 * dist + dist - num), (double) (offsetY + tkns), (double) (offsetZ + 0.0F))
                                .uv((offsetX + j2 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            buffer.vertex((double) (offsetX + j2 * dist + dist - num), (double) (offsetY + 0.0F), (double) (offsetZ + 0.0F))
                                .uv((offsetX + j2 * dist + 0.5F * dist) * uvpx + uvOrgX, (offsetZ + 0.0F) * uvpx + uvOrgZ)
                                .color(r09, g09, b09, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (blocZ > -1) { // Facing -Z
                        for (int k2 = 0; k2 < 8; ++k2) {
                            buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + tkns), (double) (offsetZ + k2 * dist + 0.0F))
                                .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + k2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            buffer.vertex((double) (offsetX + size), (double) (offsetY + tkns), (double) (offsetZ + k2 * dist + 0.0F))
                                .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + k2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            buffer.vertex((double) (offsetX + size), (double) (offsetY + 0.0F), (double) (offsetZ + k2 * dist + 0.0F))
                                .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + k2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + k2 * dist + 0.0F))
                                .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + k2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (blocZ <= 1) { // Facing +Z
                        for (int l2 = 0; l2 < 8; ++l2) {
                            buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + tkns), (double) (offsetZ + l2 * dist + dist - num))
                                .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + l2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            buffer.vertex((double) (offsetX + size), (double) (offsetY + tkns), (double) (offsetZ + l2 * dist + dist - num))
                                .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + l2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            buffer.vertex((double) (offsetX + size), (double) (offsetY + 0.0F), (double) (offsetZ + l2 * dist + dist - num))
                                .uv((offsetX + size) * uvpx + uvOrgX, (offsetZ + l2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            buffer.vertex((double) (offsetX + 0.0F), (double) (offsetY + 0.0F), (double) (offsetZ + l2 * dist + dist - num))
                                .uv((offsetX + 0.0F) * uvpx + uvOrgX, (offsetZ + l2 * dist + 0.5F * dist) * uvpx + uvOrgZ)
                                .color(r08, g08, b08, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }
                }
            }
        } else {
            int size2 = 32;

            for (int blocX1 = -size2; blocX1 < size2; blocX1 += size2) {
                for (int blocZ1 = -size2; blocZ1 < size2; blocZ1 += size2) {
                    buffer.vertex((double) (blocX1 + 0), (double) offsetY, (double) (blocZ1 + size2))
                        .uv((float) (blocX1 + 0) * uvpx + uvOrgX, (float) (blocZ1 + size2) * uvpx + uvOrgZ)
                        .color(colR, colG, colB, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    buffer.vertex((double) (blocX1 + size2), (double) offsetY, (double) (blocZ1 + size2))
                        .uv((float) (blocX1 + size2) * uvpx + uvOrgX, (float) (blocZ1 + size2) * uvpx + uvOrgZ)
                        .color(colR, colG, colB, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    buffer.vertex((double) (blocX1 + size2), (double) offsetY, (double) (blocZ1 + 0))
                        .uv((float) (blocX1 + size2) * uvpx + uvOrgX, (float) (blocZ1 + 0) * uvpx + uvOrgZ)
                        .color(colR, colG, colB, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    buffer.vertex((double) (blocX1 + 0), (double) offsetY, (double) (blocZ1 + 0))
                        .uv((float) (blocX1 + 0) * uvpx + uvOrgX, (float) (blocZ1 + 0) * uvpx + uvOrgZ)
                        .color(colR, colG, colB, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                }
            }
        }

    }


    private void bobHurt(PoseStack stack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) mc.getCameraEntity();
            float f = (float) livingentity.hurtTime - partialTicks;
            if (livingentity.isDeadOrDying()) {
                float f1 = Math.min((float) livingentity.deathTime + partialTicks, 20.0F);
                stack.mulPose(Vector3f.ZP.rotationDegrees(40.0F - 8000.0F / (f1 + 200.0F)));
            }

            if (f < 0.0F) {
                return;
            }

            f /= (float) livingentity.hurtDuration;
            f = Mth.sin(f * f * f * f * (float) Math.PI);
            float f2 = livingentity.hurtDir;
            stack.mulPose(Vector3f.YP.rotationDegrees(-f2));
            stack.mulPose(Vector3f.ZP.rotationDegrees(-f * 14.0F));
            stack.mulPose(Vector3f.YP.rotationDegrees(f2));
        }
    }

    private void bobView(PoseStack stack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof Player) {
            Player player = (Player) mc.getCameraEntity();
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * partialTicks);
            float f2 = Mth.lerp(partialTicks, player.oBob, player.bob);
            stack.translate((double) (Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F),
                    (double) (-Math.abs(Mth.cos(f1 * (float) Math.PI) * f2)), 0.0D);
            stack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
            stack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
        }
    }
}
