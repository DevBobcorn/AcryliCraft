package com.devbobcorn.sky_painter.client.window;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

// This interface is implemented by WindowMixin, so as to
// inject new methods into vanilla Window class
public interface IWindow {
    public long getGLFWId();

    public long getWindowHandle();

    public void getLWA(IntByReference pcrKey, ByteByReference pbAlpha, IntByReference pdwFlags);

    public boolean checkSetupAttempt();

    public boolean trySetupWindow();
}
