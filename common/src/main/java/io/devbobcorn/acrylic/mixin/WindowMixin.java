package io.devbobcorn.acrylic.mixin;

import org.lwjgl.sdl.SDLVideo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.client.window.IWindow;
import io.devbobcorn.acrylic.client.window.WindowUtil;

import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.backend.opengl.GlBackend;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.MonitorManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;

import net.minecraft.util.Util;

@Mixin(Window.class)
public class WindowMixin implements IWindow {

    @Final
    @Shadow
    private static Logger LOGGER;

    @Final
    @Shadow
    // SDL_Window pointer
    private long handle;

    /**
     * Add {@code SDL_WINDOW_TRANSPARENT} to the flags handed to the GPU backend,
     * which is what creates the SDL window. This is the SDL3 replacement for the
     * old {@code GLFW_TRANSPARENT_FRAMEBUFFER} window hint and works for both the
     * OpenGL and Vulkan backends.
     */
    @ModifyArg(
            method = "createWindow",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/renderpearl/api/device/GpuBackend;createWindow(Ljava/lang/String;IIJ)J",
                    remap = false
            ),
            index = 3
    )
    private long acrylic_mod$transparentWindowFlags(long flags) {
        if ((boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
            return flags | SDLVideo.SDL_WINDOW_TRANSPARENT;
        }

        return flags;
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void init(
            final WindowEventHandler handler,
            final DisplayData display, final String videoMode, final boolean fullscreen, final String title,
            final MonitorManager monitorManager, final GpuBackend gpuBackend, final int maxWindowSize,
            final CallbackInfo callback
    ) {
        // Check whether the window was created with a transparent framebuffer.
        // SDL exposes this as the SDL_WINDOW_TRANSPARENT flag on the created window.
        // See https://wiki.libsdl.org/SDL3/SDL_WindowFlags
        var transparent = (SDLVideo.SDL_GetWindowFlags(handle) & SDLVideo.SDL_WINDOW_TRANSPARENT) != 0;

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
     * Try to determine why the transparent window was not granted and record a
     * more specific hint than the generic failure message.
     *
     * The most common Linux case: even in a Wayland session, SDL may be running
     * on its X11 video driver through XWayland. SDL's X11 driver cannot create an
     * alpha-capable window for a non-OpenGL backend, so the OpenGL backend or
     * SDL's native Wayland driver must be used instead.
     */
    @Unique
    private void acrylic_mod$diagnoseTransparencyFailure(GpuBackend gpuBackend) {
        final String driver = SDLVideo.SDL_GetCurrentVideoDriver();
        final boolean x11Driver = "x11".equals(driver);

        if (x11Driver && !(gpuBackend instanceof GlBackend)) {
            AcrylicMod.setTransparencyInitFailureHintKey(AcrylicMod.MOD_ID + ".hint.transparency_x11_vulkan");
            LOGGER.warn("Acrylic: transparent window unavailable. SDL is using the X11 video driver "
                    + "(this includes XWayland on a Wayland session) together with a non-OpenGL GPU backend "
                    + "(e.g. Vulkan). SDL cannot create an alpha-capable window for such backends on X11. "
                    + "Switch to the OpenGL backend, or run on SDL's native Wayland driver "
                    + "(enable \"Prefer native Wayland\", or set SDL_VIDEO_DRIVER=wayland).");
        } else {
            AcrylicMod.setTransparencyInitFailureHintKey(AcrylicMod.MOD_ID + ".hint.transparency_init_failure");
            LOGGER.warn("Acrylic: failed to initialize transparent window (SDL video driver={}).", driver);
        }
    }

    @Override
    public long acrylic_mod$getSDLWindowHandle() {
        return handle;
    }

}
