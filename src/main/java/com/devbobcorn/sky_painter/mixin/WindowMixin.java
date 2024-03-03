package com.devbobcorn.sky_painter.mixin;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.devbobcorn.sky_painter.client.window.IWindow;
import com.devbobcorn.sky_painter.client.window.WindowUtil;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import javax.annotation.Nullable;

@Mixin(Window.class)
public class WindowMixin implements IWindow {

    @Shadow
    private static Logger LOGGER;

    @Shadow
    // GLFW Window id
    private long window;

    @Redirect(
        method = "<init>",
        remap = false, // Don't remap method name for constructors
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V",
            remap = false // Don't remap method name for native methods
        )
    )
    public void glfwWindowHintRedirect() {

        LOGGER.info("Applying default window hints...");

        GLFW.glfwDefaultWindowHints();

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

        // Magic!
        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);

        LOGGER.info("Window hints applied!");
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    public void constructWindow(WindowEventHandler h, ScreenManager sm, DisplayData displayData,
            @Nullable String videoMode, String title, CallbackInfo callbackInfo) {
        
        LOGGER.info("Window created: " + title);
    }

    @Override
    public long getGLFWId() {
        return window;
    }

    @Override
    public long getWindowHandle() {
        return WindowUtil.getWindowHandle(window);
    }

    @Override
    public void getLWA(IntByReference pcrKey, ByteByReference pbAlpha, IntByReference pdwFlags) {
        WindowUtil.getLWA(getWindowHandle(), pcrKey, pbAlpha, pdwFlags);
    }

    private boolean setupAttempt = false;

    @Override
    public boolean checkSetupAttempt() {
        return setupAttempt;
    }

    @Override
    public boolean trySetupWindow() {
        if (!setupAttempt) {
            setupAttempt = true;

            WindowUtil.setupWindow(getWindowHandle());

            return true;
        }

        return false;
    }
}
