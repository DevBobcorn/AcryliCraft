package com.devbobcorn.sky_painter.mixin;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.devbobcorn.sky_painter.client.rendering.GuiUtil;
import com.devbobcorn.sky_painter.client.rendering.ScreenshotUtil;
import com.devbobcorn.sky_painter.client.window.IWindow;
import com.devbobcorn.sky_painter.client.window.WindowUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    //private static final ResourceLocation M_TEX = new ResourceLocation("textures/gui/title/edition.png");

    private Minecraft s_minecraft = null;

    @Shadow
    private static Logger LOGGER;

    @Shadow
    private long fadeInStart;

    @SuppressWarnings("null")
    public void renderTex(PoseStack poseStack, ResourceLocation tex, int x, int y, int w, int h, int tex_w, int tex_h) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Will be set by blit() RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiUtil.blit(poseStack, x, y, 0.0F, 0.0F, w, h, tex_w, tex_h);
    }

    @SuppressWarnings("null")
    public void renderString(PoseStack poseStack, String str, int x, int y) {
        GuiComponent.drawString(poseStack, s_minecraft.font, str, x, y, 16777215);
    }

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", cancellable = true)
    public void renderHead(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();

        } else {
            // Draw debug info
            var iWindow = (IWindow) (Object) (s_minecraft.getWindow());
            var windowId = iWindow.getGLFWId();
            var windowHandle = WindowUtil.getWindowHandle(windowId);
            // See https://www.glfw.org/docs/3.3/window_guide.html#window_transparency
            boolean tb = GLFW.glfwGetWindowAttrib(windowId, GLFW.GLFW_TRANSPARENT_FRAMEBUFFER) == 1;
            float opacity = GLFW.glfwGetWindowOpacity(windowId);
            boolean setupAttempt = iWindow.checkSetupAttempt();

            var pcrKey = new IntByReference(42);
            var pbAlpha = new ByteByReference((byte) 42);
            var pdwFlags = new IntByReference((byte) 42);

            iWindow.getLWA(pcrKey, pbAlpha, pdwFlags);

            renderString(poseStack, "GLFW Window Id: " + String.valueOf(windowId) + " Window Handle: " +
                    String.format("%016X", windowHandle), 2, 2);
            renderString(poseStack, "TransparentBuffer Enabled: " + String.valueOf(tb) +
                    " Setup Attempt: " + String.valueOf(setupAttempt), 2, 12);
            renderString(poseStack, "Full-Window Opacity: " + opacity, 2, 22);

            renderString(poseStack, "pcrKey: " + pcrKey.getValue(), 2, 32);
            renderString(poseStack, "pbAlpha: " + pbAlpha.getValue(), 2, 42);
            renderString(poseStack, "pdwFlags: " + pdwFlags.getValue(), 2, 52);

            if (!setupAttempt) {
                var result = iWindow.trySetupWindow();
                LOGGER.info("Setup up window " + ( result ? "succeeded" : "failed"));
            }
        }

        var _this = (TitleScreen) (Object) this;
        //GuiUtil.fillGradient(poseStack, 0, 0, _this.width, _this.height, 400, (int) 0xFE000000, (int) 0x01000000);
        //GuiUtil.fillGradient(poseStack, 0, 0, _this.width, _this.height, 400, (int) 0xFEFFFFFF, (int) 0x80AAAAAA);

        int halfWidth  = _this.width  >> 1;
        int halfHeight = _this.height >> 1;

        GuiUtil.fillGradient(poseStack,      0,       0,   halfWidth,   halfHeight, 400, (int) 0xFF000000, (int) 0xFF000000); // L-Upper, Black Opaque
        GuiUtil.fillGradient(poseStack, halfWidth,       0, _this.width,   halfHeight, 400, (int) 0xFFFFFFFF, (int) 0xFFFFFFFF); // R-Upper, White Opaque
        GuiUtil.fillGradient(poseStack,      0, halfHeight,   halfWidth, _this.height, 400, (int) 0x80000000, (int) 0x80000000); // L-Lower, Black Transparent
        GuiUtil.fillGradient(poseStack, halfWidth, halfHeight, _this.width, _this.height, 400, (int) 0x80FFFFFF, (int) 0x80FFFFFF); // R-Lower, White Transparent

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
    public void RenderSystem_blendFuncRedirect(SourceFactor src, DestFactor dst) {
        
        RenderSystem.enableBlend();
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
    public int Mth_ceilRedirect(float value) {
        return 0;
    }
    */
}
