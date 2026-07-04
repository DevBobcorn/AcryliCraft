package io.devbobcorn.acrylic.mixin;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.client.window.IWindow;
import io.devbobcorn.acrylic.client.window.WindowUtil;

import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.MonitorManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.GpuBackend;

import net.minecraft.util.Util;

@Mixin(Window.class)
public class WindowMixin implements IWindow {

    @Final
    @Shadow
    private static Logger LOGGER;

    @Final
    @Shadow
    // GLFW Window id
    private long handle;

    @Inject(
            method = "createGlfwWindow",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/GpuBackend;setWindowHints()V",
                    remap = false,
                    shift = At.Shift.AFTER
            )
    )
    private static void glfwWindowHintInject(int width, int height, String title, long monitor, GpuBackend gpuBackend, CallbackInfoReturnable<Long> cir) {

        // Window hints applied to vanilla window:

        // 0x22001            196609
        // GLFW_FOCUSED       0x30001

        // 0x2200B            221185
        // GLFW_HOVERED       0x36001

        // 0x22002            3
        // GLFW_ICONIFIED     0x11

        // 0x22003            2
        // GLFW_RESIZABLE     0x10

        // 0x22008            204801
        // GLFW_MAXIMIZED     0x32001

        // 0x22006            1 (GLFW_TRUE)
        // GLFW_AUTO_ICONIFY  0x1

        // Initialize Acrylic by accessing its instance
        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            // Magic! (This also works on macOS and linux)
            GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
        }

        LOGGER.info("Window hints applied!");
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(
            final WindowEventHandler handler,
            final DisplayData display, final String videoMode, final boolean fullscreen, final String title,
            final MonitorManager monitorManager, final GpuBackend gpuBackend,
            final CallbackInfo callback
    ) {
        // Check if transparent frame buffer is enabled
        // See https://www.glfw.org/docs/3.3/window_guide.html#window_transparency
        var transparent = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_TRANSPARENT_FRAMEBUFFER) == 1;

        AcrylicMod.setTransparencyEnabled(transparent);

        var config = AcrylicConfig.getInstance();

        // Re-evaluate on every window creation (e.g. Vulkan may fall back to OpenGL).
        if ((boolean) config.getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            if (transparent) {
                AcrylicMod.setTransparencyInitFailed(false);
                AcrylicMod.setTransparencyInitFailureHintKey(AcrylicMod.MOD_ID + ".hint.transparency_init_failure");
            } else {
                AcrylicMod.setTransparencyInitFailed(true);
                acrylic_mod$diagnoseTransparencyFailure(gpuBackend);
            }
        }

        // Check OS
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            // Store window handle for later use
            AcrylicMod.setWindowHandle(WindowUtil.getWindowHandle(handle));

            // Apply Win11-Specific window setup
            config.ApplyWin11Specific();
        }
    }

    /**
     * Try to determine why the transparent framebuffer was not granted and record a
     * more specific hint than the generic failure message.
     *
     * The most common Linux case: even in a Wayland session, the game's bundled GLFW
     * runs on its X11 backend through XWayland by default. On the X11 backend GLFW only
     * selects an alpha-capable (ARGB) X visual on the GLX/EGL context-creation path; a
     * window created for a non-OpenGL backend uses GLFW_NO_API as its client API and
     * therefore falls back to the opaque DefaultVisual, so GLFW_TRANSPARENT_FRAMEBUFFER
     * can never be honoured. The OpenGL backend, or running on GLFW's native Wayland
     * backend (system GLFW 3.4+ via -Dorg.lwjgl.glfw.libname), do not have this problem.
     */
    @Unique
    private void acrylic_mod$diagnoseTransparencyFailure(GpuBackend gpuBackend) {
        final int platform = GLFW.glfwGetPlatform();

        // A NO_API window means a non-OpenGL backend (e.g. Minecraft 26.2's Vulkan backend).
        final boolean noApiWindow = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_CLIENT_API) == GLFW.GLFW_NO_API;

        if (platform == GLFW.GLFW_PLATFORM_X11 && noApiWindow && !(gpuBackend instanceof GlBackend)) {
            AcrylicMod.setTransparencyInitFailureHintKey(AcrylicMod.MOD_ID + ".hint.transparency_x11_vulkan");
            LOGGER.warn("Acrylic: transparent framebuffer unavailable. GLFW is using the X11 backend "
                    + "(this includes XWayland on a Wayland session) together with a non-OpenGL GPU backend "
                    + "(GLFW_NO_API, e.g. Vulkan). GLFW cannot create an alpha-capable framebuffer for such "
                    + "windows on X11. Switch to the OpenGL backend, or run on GLFW's native Wayland backend "
                    + "(system GLFW 3.4+ via -Dorg.lwjgl.glfw.libname).");
        } else {
            AcrylicMod.setTransparencyInitFailureHintKey(AcrylicMod.MOD_ID + ".hint.transparency_init_failure");
            LOGGER.warn("Acrylic: failed to initialize transparent framebuffer (GLFW platform={}, GLFW_NO_API window={}).",
                    platform, noApiWindow);
        }
    }

    @Override
    public long acrylic_mod$getGLFWId() {
        return handle;
    }

}
