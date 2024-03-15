package io.devbobcorn.acrylic;

public class AcrylicMod {

    public static final String MOD_ID = "acrylic";

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
}
