package com.devbobcorn.sky_painter.client.window;

import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.BLENDFUNCTION;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

// See https://dynamicwallpaper.readthedocs.io/en/docs/dev/make-wallpaper.html
public class WindowUtil {
    
    public static void getLWA(long handle, IntByReference pcrKey, ByteByReference pbAlpha, IntByReference pdwFlags) {
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(handle));

        User32.INSTANCE.GetLayeredWindowAttributes(hwnd, pcrKey, pbAlpha, pdwFlags);
    }

    public static void setupWindow(long handle) {
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(handle));

        User32.INSTANCE.SetLayeredWindowAttributes(hwnd, 0xFF0000, (byte) 0, WinUser.LWA_COLORKEY);

        BLENDFUNCTION blend = new BLENDFUNCTION();
        blend.SourceConstantAlpha = (byte) 255;
        blend.AlphaFormat = WinUser.AC_SRC_ALPHA;

        User32.INSTANCE.UpdateLayeredWindow(hwnd, null, null,
                null, null, null, 0xFF0000, blend, WinUser.ULW_COLORKEY);
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
