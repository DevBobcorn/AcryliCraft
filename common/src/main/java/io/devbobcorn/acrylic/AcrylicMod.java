package io.devbobcorn.acrylic;

import com.mojang.logging.LogUtils;
import com.sun.jna.ptr.IntByReference;

import io.devbobcorn.acrylic.client.screen.ConfigScreenUtil;
import io.devbobcorn.acrylic.nativelib.NtDllLib;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import static net.minecraft.network.chat.Component.translatable;
import org.slf4j.Logger;

public class AcrylicMod {

    public static final String MOD_ID = "acrylic";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private static long windowHandle = 0;

    public static long getWindowHandle() {
        return windowHandle;
    }

    public static void setWindowHandle(long handle) throws IllegalStateException {
        if (windowHandle != 0) {
            throw new IllegalStateException("Window handle is already assigned!");
        }

        windowHandle = handle;
    }

    public static void init() {
        //System.out.println("Hello, Acrylic!");

    }
}
