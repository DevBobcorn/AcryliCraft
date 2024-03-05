package com.devbobcorn.acrylic;

import java.nio.file.Path;
import java.util.Properties;

import com.devbobcorn.acrylic.nativelib.DwmApiLib;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.Settings;
import net.minecraftforge.fml.loading.FMLPaths;

public class AcrylicConfig extends Settings<AcrylicConfig> {
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(AcrylicMod.MODID + ".json");
    
    private static AcrylicConfig instance;

    public static AcrylicConfig getInstance() {
        if (instance == null) {
            instance = new AcrylicConfig();
        }

        return instance;
    }

    public final Settings<AcrylicConfig>.MutableValue<Boolean> useImmersiveDarkMode;
    public final Settings<AcrylicConfig>.MutableValue<DwmApiLib.DWM_SYSTEMBACKDROP_TYPE> systemBackdropType;
    public final Settings<AcrylicConfig>.MutableValue<DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE> windowCorner;


    @SuppressWarnings("null")
    private AcrylicConfig() {
        this(Settings.loadFromFile(CONFIG_PATH));
    }

    private AcrylicConfig(final Properties properties) {

        super(properties);

        // DWMWA_USE_IMMERSIVE_DARK_MODE
        this.useImmersiveDarkMode = this.getMutable("use-immersive-dark-mode", Boolean::parseBoolean, false);
        
        // DWMWA_SYSTEMBACKDROP_TYPE
        this.systemBackdropType = this.getMutable("system-backdrop-type", value -> {
            try { return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.valueOf(value); }
            catch (final IllegalArgumentException ignored) {
                return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO;
            }
        }, DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO);

        // DWMWA_WINDOW_CORNER_PREFERENCE
        this.windowCorner = this.getMutable("window-corner-preference", value -> {
            try { return DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.valueOf(value); }
            catch (final IllegalArgumentException ignored) {
                return DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT;
            }
        }, DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT);

        if (!AcrylicMod.checkCompatible()) {
            return;
        }

        long handle = AcrylicMod.getWindowHandle();

        // Apply stored configs
        DwmApiLib.setBooleanWA(handle, DwmApiLib.DWM_BOOL_WA
                .DWMWA_USE_IMMERSIVE_DARK_MODE, useImmersiveDarkMode.get());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA
                .DWMWA_SYSTEMBACKDROP_TYPE, systemBackdropType.get());
        
        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA
                .DWMWA_WINDOW_CORNER_PREFERENCE, windowCorner.get());
    }

    /* ======================================== */

    @SuppressWarnings("null")
    @Override
    protected AcrylicConfig reload(RegistryAccess registryAccess, Properties properties) {
        instance = new AcrylicConfig(properties);
        instance.store(CONFIG_PATH);
        return getInstance();
    }

    @SuppressWarnings("null")
    public <T> void setConfig(Settings<AcrylicConfig>.MutableValue<T> config, T value) {

        config.update(null, value);

        if (!AcrylicMod.checkCompatible()) {
            return;
        }

        long handle = AcrylicMod.getWindowHandle();
        
        if (config == useImmersiveDarkMode) {
            DwmApiLib.setBooleanWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE, (boolean) value);
        }

        if (config == systemBackdropType) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                    (DwmApiLib.DWM_SYSTEMBACKDROP_TYPE) value);
        }

        if (config == windowCorner) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                    (DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE) value);
        }

    }
}
