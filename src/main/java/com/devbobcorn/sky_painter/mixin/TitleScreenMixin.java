package com.devbobcorn.sky_painter.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.devbobcorn.sky_painter.client.GuiUtil;
import com.devbobcorn.sky_painter.client.ScreenshotUtil;
import com.devbobcorn.sky_painter.client.WindowHandle;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    //private static final ResourceLocation M_TEX = new ResourceLocation("textures/gui/title/edition.png");

    private Minecraft s_minecraft = null;

    @Shadow
    private long fadeInStart;

    @SuppressWarnings("null")
    public void renderTex(PoseStack poseStack, ResourceLocation tex, int x, int y, int w, int h, int tex_w, int tex_h)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Will be set by blit() RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiUtil.blit(poseStack, x, y, 0.0F, 0.0F, w, h, tex_w, tex_h);
    }

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", cancellable = true)
    @SuppressWarnings("null")
    public void renderHead(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();

        } else {
            // Draw a string for debugging
            var windowId = ( (WindowHandle) (Object) (s_minecraft.getWindow()) ).GetGLFWId();

            GuiComponent.drawString(poseStack, s_minecraft.font, "GLFW Window Id: " + String.valueOf(windowId), 0, 0, 16777215);
        }

        //var _this = (TitleScreen) (Object) this;
        //GuiUtil.fillGradient(poseStack, 0, 0, _this.width, _this.height, 400, (int) 0xFE000000, (int) 0x01000000);
        //GuiUtil.fillGradient(poseStack, 0, 0, _this.width, _this.height, 400, (int) 0xFEFFFFFF, (int) 0x80AAAAAA);

        //renderTex(poseStack, M_TEX, mouseX, mouseY, 98, 14, 128, 16);
    }

    private int renderedFrames = 0;

    // Grab the main framebuffer with transparency after rendering some frames
    @Inject(at = @At("RETURN"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", cancellable = true)
    public void renderReturn(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        renderedFrames += 1;

        if (renderedFrames == 150) {
            ScreenshotUtil.grabWithAlpha(s_minecraft.gameDirectory, "magic.png", s_minecraft.getMainRenderTarget());
        }
    }

    /*
    // Modify blendFunc before rendering
    @Redirect(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"
        )
    )
    public void RenderSystem_blendFuncRedirect(SourceFactor src, DestFactor dst)
    {
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
    */

    /*
    // Disable all gui components on Title Screen
    @Redirect(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
        at = @At(
            value = "INVOKE",
            ordinal = 1, // Match the 2nd appearance only
            target = "Lnet/minecraft/util/Mth;ceil(F)I"
        )
    )
    public int Mth_ceilRedirect(float value)
    {
        return 0;
    }
    */
}
