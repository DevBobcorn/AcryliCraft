package io.devbobcorn.acrylic.client.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;

public class WindowUtil {

    public static String getGlfwPlatformName() {
        final int platform = GLFW.glfwGetPlatform();
        return switch (platform) {
            case GLFW.GLFW_PLATFORM_WIN32 -> "Win32";
            case GLFW.GLFW_PLATFORM_COCOA -> "Cocoa";
            case GLFW.GLFW_PLATFORM_WAYLAND -> "Wayland";
            case GLFW.GLFW_PLATFORM_X11 -> "X11";
            case GLFW.GLFW_PLATFORM_NULL -> "Null";
            default -> "Unknown (0x" + Integer.toHexString(platform) + ")";
        };
    }

    // Get window handle (HWND) from GLFW window id
    public static long getWindowHandle(long windowId) {
        if (Platform.get() == Platform.WINDOWS) {
            return innerGetWindowHandle(windowId);
        } else {
            // Return 0 for non-Windows platforms
            return 0L;
        }
    }

    private static long innerGetWindowHandle(long windowId) {

        return GLFWNativeWin32.glfwGetWin32Window(windowId);
    }

}
