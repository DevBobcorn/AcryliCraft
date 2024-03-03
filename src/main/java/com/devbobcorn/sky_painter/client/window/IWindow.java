package com.devbobcorn.sky_painter.client.window;

// This interface is implemented by WindowMixin, so as to
// inject new methods into vanilla Window class
public interface IWindow {
    public long getGLFWId();

    public long getWindowHandle();

    public boolean checkSetupAttempt();

    public boolean trySetupWindow();
}
