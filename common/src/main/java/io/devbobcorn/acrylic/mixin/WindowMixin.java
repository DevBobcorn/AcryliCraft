package io.devbobcorn.acrylic.mixin;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.client.window.IWindow;
import io.devbobcorn.acrylic.client.window.WindowUtil;

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
        if (AcrylicConfig.getInstance().getValue(AcrylicConfig.TRANSPARENT_WINDOW)) {
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

        // Check if transparent framebuffer requested but failed to initialize
        if ((boolean) config.getValue(AcrylicConfig.TRANSPARENT_WINDOW) && !transparent) {
            AcrylicMod.setTransparencyInitFailed(true);
        }

        // Check OS
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            // Store window handle for later use
            AcrylicMod.setWindowHandle(WindowUtil.getWindowHandle(handle));

            // Apply Win11-Specific window setup
            config.ApplyWin11Specific();
        }
    }

    @Override
    public long acrylic_mod$getGLFWId() {
        return handle;
    }

}
