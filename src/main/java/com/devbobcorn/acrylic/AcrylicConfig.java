package com.devbobcorn.acrylic;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;

import com.devbobcorn.acrylic.nativelib.DwmApiLib;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.Settings;
import net.minecraftforge.fml.loading.FMLPaths;

public class AcrylicConfig extends Settings<AcrylicConfig> {
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(AcrylicMod.MODID + ".ini");
    
    private static AcrylicConfig instance;

    public static AcrylicConfig getInstance() {
        if (instance == null) {
            instance = new AcrylicConfig();
        }

        return instance;
    }

    public static final String USE_IMMERSIVE_DARK_MODE   = "use_immersive_dark_mode";
    public static final String SYSTEM_BACKDROP_TYPE      = "system_backdrop_type";
    public static final String WINDOW_CORNER_PREFERENCE  = "window_corner_preference";
    public static final String CUSTOMIZE_BORDER          = "customize_border";
    public static final String HIDE_BORDER               = "hide_border";
    public static final String BORDER_COLOR              = "border_color";
    public static final String CUSTOMIZE_CAPTION         = "customize_caption";
    public static final String CAPTION_COLOR             = "caption_color";
    public static final String CUSTOMIZE_TEXT            = "customize_text";
    public static final String TEXT_COLOR                = "text_color";

    private final Hashtable<String, Settings<AcrylicConfig>.MutableValue<?>> configValues = new Hashtable<>();

    @SuppressWarnings("null")
    private AcrylicConfig() {
        this(Settings.loadFromFile(CONFIG_PATH));
    }

    private AcrylicConfig(final Properties properties) {

        super(properties);

        configValues.put( USE_IMMERSIVE_DARK_MODE,
            this.getMutable(USE_IMMERSIVE_DARK_MODE, Boolean::parseBoolean, false)
        );
        
        configValues.put( SYSTEM_BACKDROP_TYPE,
            this.getMutable(SYSTEM_BACKDROP_TYPE, value -> {
                try { return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.valueOf(value); }
                catch (final IllegalArgumentException ignored) {
                    return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO;
                }
            }, DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO)
        );

        configValues.put( WINDOW_CORNER_PREFERENCE,
            this.getMutable(WINDOW_CORNER_PREFERENCE, value -> {
                try { return DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.valueOf(value); }
                catch (final IllegalArgumentException ignored) {
                    return DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT;
                }
            }, DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT)
        );

        configValues.put( CUSTOMIZE_BORDER,
            this.getMutable(CUSTOMIZE_BORDER, Boolean::parseBoolean, false)
        );
        configValues.put( HIDE_BORDER,
            this.getMutable(HIDE_BORDER, Boolean::parseBoolean, false)
        );
        configValues.put( BORDER_COLOR,
            this.getMutable(BORDER_COLOR, Integer::parseInt, DwmApiLib.RGB_BLACK)
        );

        configValues.put( CUSTOMIZE_CAPTION,
            this.getMutable(CUSTOMIZE_CAPTION, Boolean::parseBoolean, false)
        );
        configValues.put( CAPTION_COLOR,
            this.getMutable(CAPTION_COLOR, Integer::parseInt, DwmApiLib.DWMWA_COLOR_DEFAULT)
        );

        configValues.put( CUSTOMIZE_TEXT,
            this.getMutable(CUSTOMIZE_TEXT, Boolean::parseBoolean, false)
        );
        configValues.put( TEXT_COLOR,
            this.getMutable(TEXT_COLOR, Integer::parseInt, DwmApiLib.DWMWA_COLOR_DEFAULT)
        );

        if (!AcrylicMod.checkCompatible()) {
            return;
        }

        long handle = AcrylicMod.getWindowHandle();

        // Apply stored configs
        DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE,
                (boolean) configValues.get(USE_IMMERSIVE_DARK_MODE).get());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                (DwmApiLib.DWM_SYSTEMBACKDROP_TYPE) configValues.get(SYSTEM_BACKDROP_TYPE).get());
        
        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                (DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE) configValues.get(WINDOW_CORNER_PREFERENCE).get());
    }

    /* ======================================== */

    @SuppressWarnings("null")
    @Override
    protected AcrylicConfig reload(RegistryAccess registryAccess, Properties properties) {
        instance = new AcrylicConfig(properties);
        instance.store(CONFIG_PATH);
        return getInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) configValues.get(key).get();
    }

    @SuppressWarnings({ "null", "unchecked" })
    public <T> void setValue(String key, T value) {

        if (!AcrylicMod.checkCompatible()) {
            return;
        }

        ( (MutableValue<T>) configValues.get(key) ).update(null, value);

        long handle = AcrylicMod.getWindowHandle();
        
        if (key == USE_IMMERSIVE_DARK_MODE) {
            DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE, (boolean) value);
        }

        if (key == SYSTEM_BACKDROP_TYPE) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                    (DwmApiLib.DWM_SYSTEMBACKDROP_TYPE) value);
        }

        if (key == WINDOW_CORNER_PREFERENCE) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                    (DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE) value);
        }

    }
}
