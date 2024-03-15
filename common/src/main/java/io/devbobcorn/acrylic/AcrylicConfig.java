package io.devbobcorn.acrylic;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;

import dev.architectury.platform.Platform;
import io.devbobcorn.acrylic.nativelib.DwmApiLib;

import io.devbobcorn.acrylic.nativelib.NtDllLib;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.Settings;
import org.jetbrains.annotations.NotNull;

public class AcrylicConfig extends Settings<AcrylicConfig> {
    public static final Path CONFIG_PATH = Platform.getConfigFolder().resolve(AcrylicMod.MOD_ID + ".ini");

    private static AcrylicConfig instance;

    public static AcrylicConfig getInstance() {
        if (instance == null) {
            instance = new AcrylicConfig();
        }

        return instance;
    }

    public static final String SHOW_DEBUG_INFO           = "show_debug_info";

    public static final String USE_IMMERSIVE_DARK_MODE   = "use_immersive_dark_mode";
    public static final String SYSTEM_BACKDROP_TYPE      = "system_backdrop_type";
    public static final String WINDOW_CORNER_PREFERENCE  = "window_corner_preference";
    public static final String CUSTOMIZE_BORDER          = "customize_border";
    public static final String HIDE_BORDER               = "hide_border";
    public static final String BORDER_COLOR              = "border_color";
    //public static final String CUSTOMIZE_CAPTION         = "customize_caption";
    //public static final String CAPTION_COLOR             = "caption_color";
    //public static final String CUSTOMIZE_TEXT            = "customize_text";
    //public static final String TEXT_COLOR                = "text_color";

    private final Hashtable<String, Settings<AcrylicConfig>.MutableValue<?>> configValues = new Hashtable<>();

    @SuppressWarnings("null")
    private AcrylicConfig() {

        this(Settings.loadFromFile(CONFIG_PATH));

        if (!NtDllLib.checkCompatibility()) {
            return;
        }

        long handle = AcrylicMod.getWindowHandle();

        //User32Lib.TingeWindow(handle, 0x3FBBC539);

        // Apply stored configs
        DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE,
                (boolean) configValues.get(USE_IMMERSIVE_DARK_MODE).get());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                (DwmApiLib.DWM_SYSTEMBACKDROP_TYPE) configValues.get(SYSTEM_BACKDROP_TYPE).get());

        DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                (DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE) configValues.get(WINDOW_CORNER_PREFERENCE).get());

        var borderHidden = (boolean) getValue(HIDE_BORDER);
        var customBorder = (boolean) getValue(CUSTOMIZE_BORDER);

        if (borderHidden) {
            // Window border is hidden
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_NONE);
        } else if (customBorder) {
            // Window border is visible and customized
            var borderRgb = (int) getValue(BORDER_COLOR);
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.rgb2ColorRef(borderRgb));
        } else {
            // Use default border color
            DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_DEFAULT);
        }

    }

    private AcrylicConfig(final Properties properties) {

        super(properties);

        configValues.put( SHOW_DEBUG_INFO,
                this.getMutable(SHOW_DEBUG_INFO, Boolean::parseBoolean, false)
        );

        configValues.put( USE_IMMERSIVE_DARK_MODE,
                this.getMutable(USE_IMMERSIVE_DARK_MODE, Boolean::parseBoolean, false)
        );

        configValues.put( SYSTEM_BACKDROP_TYPE,
                this.getMutable(SYSTEM_BACKDROP_TYPE, value -> {
                    try { return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.valueOf(value); }
                    catch (final IllegalArgumentException ignored) {
                        return DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW;
                    }
                }, DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW)
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
                this.getMutable(BORDER_COLOR, Integer::parseInt, DwmApiLib.COLOR_BLACK.getRGB())
        );

    }

    /* ======================================== */

    @SuppressWarnings("null")
    @Override
    protected @NotNull AcrylicConfig reload(RegistryAccess registryAccess, Properties properties) {
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

        if (!NtDllLib.checkCompatibility()) {
            return;
        }

        // Update the value in config
        ( (MutableValue<T>) configValues.get(key) ).update(null, value);

        // Then reflect value changes on the window
        long handle = AcrylicMod.getWindowHandle();

        if (key.equals(USE_IMMERSIVE_DARK_MODE)) {
            DwmApiLib.setBoolWA(handle, DwmApiLib.DWM_BOOL_WA.DWMWA_USE_IMMERSIVE_DARK_MODE, (boolean) value);
        }

        if (key.equals(SYSTEM_BACKDROP_TYPE)) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                    (DwmApiLib.DWM_SYSTEMBACKDROP_TYPE) value);
        }

        if (key.equals(WINDOW_CORNER_PREFERENCE)) {
            DwmApiLib.setEnumWA(handle, DwmApiLib.DWM_ENUM_WA.DWMWA_WINDOW_CORNER_PREFERENCE,
                    (DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE) value);
        }

        if (key.equals(HIDE_BORDER) || key.equals(CUSTOMIZE_BORDER) || key.equals(BORDER_COLOR)) {

            var borderHidden = (boolean) (key.equals(HIDE_BORDER) ? value : getValue(HIDE_BORDER) );
            var customBorder = (boolean) (key.equals(CUSTOMIZE_BORDER) ? value : getValue(CUSTOMIZE_BORDER) );

            if (borderHidden) {
                // Window border is hidden
                DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_NONE);
            } else if (customBorder) {
                // Window border is visible and customized
                int borderRgb = (int) (key.equals(BORDER_COLOR) ? value : getValue(BORDER_COLOR) );
                DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.rgb2ColorRef(borderRgb));
            } else {
                // Use default border color
                DwmApiLib.setIntWA(handle, DwmApiLib.DWM_INT_WA.DWMWA_BORDER_COLOR, DwmApiLib.DWMWA_COLOR_DEFAULT);
            }
        }

    }
}
