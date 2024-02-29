package com.devbobcorn.sky_painter.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", cancellable = true)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        //RenderSystem.clearColor(0x39 / 255F, 0xC5 / 255F, 0xBB / 255F, 1);
        RenderSystem.clearColor(0, 0, 0, 0F);
        //RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
    }
}
