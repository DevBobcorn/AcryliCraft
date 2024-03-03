package com.devbobcorn.sky_painter.client.window;

// This interface is implemented by WindowMixin, so as to
// inject new methods into vanilla Window class
public interface WindowHandle {
    public long GetGLFWId();
}
