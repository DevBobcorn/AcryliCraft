package io.devbobcorn.acrylic.client.window;

import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.Platform;

public class WindowUtil {

    /**
     * Name of the SDL video driver currently in use (e.g. "windows", "x11",
     * "wayland" or "cocoa"), or "Unknown" before SDL has been initialized.
     */
    public static String getPlatformName() {
        final String driver = SDLVideo.SDL_GetCurrentVideoDriver();
        return driver != null ? driver : "Unknown";
    }

    // Get the native window handle (HWND) from an SDL window pointer.
    public static long getWindowHandle(long windowId) {
        if (Platform.get() == Platform.WINDOWS) {
            return innerGetWindowHandle(windowId);
        } else {
            // Return 0 for non-Windows platforms
            return 0L;
        }
    }

    private static long innerGetWindowHandle(long windowId) {

        final int properties = SDLVideo.SDL_GetWindowProperties(windowId);

        if (properties == 0) {
            return 0L;
        }

        return SDLProperties.SDL_GetPointerProperty(
                properties, SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER, 0L);
    }

}
