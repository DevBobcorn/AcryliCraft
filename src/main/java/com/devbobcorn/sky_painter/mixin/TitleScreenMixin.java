package com.devbobcorn.sky_painter.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.devbobcorn.sky_painter.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final ResourceLocation M_LOGO = new ResourceLocation(ExampleMod.MODID, "textures/gui/kamitsubaki.png");
    
    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", cancellable = true)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        var _this = (TitleScreen) (Object) this;

        //RenderSystem.clearColor(0x39 / 255F, 0xC5 / 255F, 0xBB / 255F, 1);
        RenderSystem.clearColor(1, 1, 1, 0.5F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);

        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, M_LOGO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        _this.blit(poseStack, 0, 0, 0, 0, 128, 128);
    }
}
