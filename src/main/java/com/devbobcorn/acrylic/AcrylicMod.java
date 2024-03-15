package com.devbobcorn.acrylic;

import com.devbobcorn.acrylic.client.screen.ConfigScreenUtil;
import com.devbobcorn.acrylic.nativelib.NtDllLib;
import com.sun.jna.ptr.IntByReference;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import static net.minecraft.network.chat.Component.translatable;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AcrylicMod.MOD_ID)
public class AcrylicMod
{
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

    public AcrylicMod()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO from common setup");

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
            return new ConfigScreenHandler.ConfigScreenFactory((client, screen) -> AcrylicMod.configScreen(screen));
        });
    }

    public static boolean checkCompatible() {
        var major = new IntByReference();
        var build = new IntByReference();
        NtDllLib.getBuildNumber(major, build);

        return major.getValue() >= 10 && (build.getValue() & 0x0FFFFFFF) >= 22621;
    }

    /**
     * Create a config screen.
     *
     * @param screen previous screen
     * @return config screen
     */
    @SuppressWarnings("null")
    public static Screen configScreen(final Screen screen) {
        
        // Check OS compatibility
        if (!checkCompatible()) {
            return new AlertScreen(
                () -> Minecraft.getInstance().setScreen(screen),
                translatable("acrylic.unsupported.title").withStyle(ChatFormatting.BOLD, ChatFormatting.RED),
                translatable("acrylic.unsupported.description")
            );
        }

        // Create Mod Config screen
        return ConfigScreenUtil.create(screen);
    }
}
