package io.devbobcorn.acrylic;

public class AcrylicMod {

    public static final String MOD_ID = "acrylic";

    private static long windowHandle = 0;

    private static boolean transparencyEnabled = false;

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

    /**
     * Whether alpha channel of main render target should be
     * filled with 1 (fully opaque).
     * This is required to render alpha-blended frames properly
     * when transparent window is enabled.
     */
    private static boolean fillMainRTAlpha = true;

    /**
     * Get the flag indicating whether main render
     * target should be filled opaque.
     */
    public static boolean getFillMainRTAlpha() {
        return fillMainRTAlpha;
    }

    /**
     * Set the flag indicating whether main render
     * target should be filled opaque.
     */
    public static void setFillMainRTAlpha(boolean fill) {
        fillMainRTAlpha = fill;
    }

}
