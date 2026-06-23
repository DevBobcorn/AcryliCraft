package io.devbobcorn.acrylic;

public class AcrylicMod {

    public static final String MOD_ID = "acrylic";

    private static long windowHandle = 0;

    private static boolean transparencyEnabled = false;

    private static boolean transparencyInitFailed = false;

    /**
     *  Translation key for the message shown when transparency initialization
     *  failed. Defaults to the generic hint and may be refined to a more
     *  specific reason (e.g. the X11/Vulkan limitation) upon window creation.
     */
    private static String transparencyInitFailureHintKey = MOD_ID + ".hint.transparency_init_failure";

    /**
     *  Check whether window transparency is enabled.
     */
    public static boolean getTransparencyEnabled() {
        return transparencyEnabled;
    }

    /**
     *  Record whether window transparency is enabled.
     *  This is supposed to be called only once, upon window creation.
     */
    public static void setTransparencyEnabled(boolean transparent) {
        transparencyEnabled = transparent;
    }

    /**
     *  Check whether window transparency initialization failed.
     */
    public static boolean getTransparencyInitFailed() {
        return transparencyInitFailed;
    }

    /**
     *  Record whether window transparency initialization failed.
     *  This is supposed to be called only once, upon window creation.
     */
    public static void setTransparencyInitFailed(boolean failed) {
        transparencyInitFailed = failed;
    }

    /**
     *  Get the translation key describing why transparency initialization failed.
     */
    public static String getTransparencyInitFailureHintKey() {
        return transparencyInitFailureHintKey;
    }

    /**
     *  Set the translation key describing why transparency initialization failed.
     *  This is supposed to be called only once, upon window creation.
     */
    public static void setTransparencyInitFailureHintKey(String key) {
        transparencyInitFailureHintKey = key;
    }

    /**
     *  Get the handle to the game window.
     */
    public static long getWindowHandle() {
        return windowHandle;
    }

    /**
     *  Set the handle to the game window.
     *  This may only be invoked once.
     */
    public static void setWindowHandle(long handle) throws IllegalStateException {
        if (windowHandle != 0) {
            throw new IllegalStateException("Window handle is already assigned!");
        }

        windowHandle = handle;
    }

}
