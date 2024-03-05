package com.devbobcorn.acrylic.client.window;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// This interface is implemented by WindowMixin, so as to
// inject new methods into vanilla Window class
@OnlyIn(Dist.CLIENT)
public interface IWindow {
    public long getGLFWId();

    public long getWindowHandle();

    public boolean checkSetupAttempt();

    public boolean trySetupWindow();
}
