package io.devbobcorn.acrylic.mixin;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.client.rendering.ScreenshotUtil;
import io.devbobcorn.acrylic.client.window.IWindow;
import io.devbobcorn.acrylic.client.window.WindowUtil;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    //private static final ResourceLocation M_TEX = new ResourceLocation("textures/gui/title/edition.png");

    private Minecraft s_minecraft = null;

    @Shadow
    private static Logger LOGGER;

    @Shadow
    private long fadeInStart;

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

            if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.SHOW_DEBUG_INFO)) {
                // Draw debug info
                var iWindow = (IWindow) (Object) (s_minecraft.getWindow());
                var windowId = iWindow.getGLFWId();
                var windowHandle = WindowUtil.getWindowHandle(windowId);
                // See https://www.glfw.org/docs/3.3/window_guide.html#window_transparency
                boolean tb = GLFW.glfwGetWindowAttrib(windowId, GLFW.GLFW_TRANSPARENT_FRAMEBUFFER) == 1;

                renderString(poseStack, "Window Handle: " + String.format("0x%016X", windowHandle) +
                        " (TransparentBuffer Enabled: " + String.valueOf(tb) + ")", 2, 2);
                renderString(poseStack, GlUtil.getRenderer() + ", OpenGL " + GlUtil.getOpenGLVersion(), 2, 12);
            }
        }

        /*
        var _this = (TitleScreen) (Object) this;

        int halfWidth  = _this.width  >> 1;
        int halfHeight = _this.height >> 1;

        GuiUtil.fillGradient(poseStack,      0,       0,   halfWidth,   halfHeight, 400, (int) 0xFF000000, (int) 0xFF000000); // L-Upper, Black Opaque
        GuiUtil.fillGradient(poseStack, halfWidth,       0, _this.width,   halfHeight, 400, (int) 0xFFFFFFFF, (int) 0xFFFFFFFF); // R-Upper, White Opaque
        GuiUtil.fillGradient(poseStack,      0, halfHeight,   halfWidth, _this.height, 400, (int) 0x80000000, (int) 0x80000000); // L-Lower, Black Transparent
        GuiUtil.fillGradient(poseStack, halfWidth, halfHeight, _this.width, _this.height, 400, (int) 0x80FFFFFF, (int) 0x80FFFFFF); // R-Lower, White Transparent
        */

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
}
