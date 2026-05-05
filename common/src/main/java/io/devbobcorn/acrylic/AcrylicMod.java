package io.devbobcorn.acrylic;

public class AcrylicMod {

    public static final String MOD_ID = "acrylic";

    private static long windowHandle = 0;

    private static boolean transparencyEnabled = false;

    private static boolean transparencyInitFailed = false;

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
