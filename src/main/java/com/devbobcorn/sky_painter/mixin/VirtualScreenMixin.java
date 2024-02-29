package com.devbobcorn.sky_painter.mixin;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;

@Mixin(VirtualScreen.class)
public class VirtualScreenMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    private Minecraft minecraft;

    @Shadow
    private ScreenManager screenManager;

    // Method call
    // newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;

    // Constructor for Lcom/mojang/blaze3d/platform/Window;
    // (Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;

    @Redirect(
        method = "newWindow",
        at = @At(
            value = "NEW",
            target = "(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"
        )
    )
    public Window windowFactory(WindowEventHandler h, ScreenManager sm, DisplayData displayData, @Nullable String videoMode, String title) {

        LOGGER.info("Constructing a new window...");

        return new Window(this.minecraft, this.screenManager, displayData, videoMode, title);
    }

    @Inject(
        method = "newWindow",
        at = @At(
            value = "NEW",
            target = "(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"
        )
    )
    public void newWindow(DisplayData displayData, @Nullable String videoMode, String title, CallbackInfoReturnable<Window> callback) {

        LOGGER.info("Creating main window...");
    }
}
