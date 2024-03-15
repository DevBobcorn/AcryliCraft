package io.devbobcorn.acrylic.client.window;

import io.devbobcorn.acrylic.nativelib.DwmApiLib;
import io.devbobcorn.acrylic.nativelib.DwmApiLib.DWM_ENUM_WA;
import io.devbobcorn.acrylic.nativelib.DwmApiLib.DWM_SYSTEMBACKDROP_TYPE;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;

public class WindowUtil {

    public static void setupWindow(long handle) {

        DwmApiLib.setEnumWA(handle, DWM_ENUM_WA.DWMWA_SYSTEMBACKDROP_TYPE,
                DWM_SYSTEMBACKDROP_TYPE.DWMSBT_TRANSIENTWINDOW);
    }

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
