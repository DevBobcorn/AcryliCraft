package io.devbobcorn.acrylic.client.window;

import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;

public class WindowUtil {

    // Get window handle (HWND) from GLFW window id
    public static long getWindowHandle(long windowId) {
        if (Platform.get() == Platform.WINDOWS) {
            return innerGetWindowHandle(windowId);
        } else {
            throw new UnsupportedOperationException("Only Windows is supported!");
        }
    }

    private static long innerGetWindowHandle(long windowId) {
        long handle = GLFWNativeWin32.glfwGetWin32Window(windowId);

        return handle;
    }
}
