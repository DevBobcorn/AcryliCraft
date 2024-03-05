package com.devbobcorn.acrylic.client.rendering;

import java.io.File;
import org.slf4j.Logger;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;

import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenshotUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static NativeImage takeScreenshotWithAlpha(RenderTarget rt) {
        int w = rt.width;
        int h = rt.height;
        NativeImage nativeimage = new NativeImage(w, h, false);
        RenderSystem.bindTexture(rt.getColorTextureId());

        //nativeimage.downloadTexture(0, true);
        // The second parameter fills all alpha values to 255
        nativeimage.downloadTexture(0, false);

        nativeimage.flipY();

        return nativeimage;
    }

    @SuppressWarnings("null")
    public static void grabWithAlpha(File mcDir, String name, RenderTarget rt) {
        NativeImage nativeimage = takeScreenshotWithAlpha(rt);
        File file1 = new File(mcDir, "screenshots");
        file1.mkdir();
        File file2;
        
        file2 = new File(file1, name);

        var event = net.minecraftforge.client.ForgeHooksClient.onScreenshot(nativeimage, file2);
        if (event.isCanceled()) {
            return;
        }
        
        final File target = event.getScreenshotFile();

        Util.ioPool().execute(() -> {
            try {
                nativeimage.writeToFile(target);
                LOGGER.info("Saved screenshot with alpha to " + target.toPath());
            } catch (Exception exception) {
                LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
            } finally {
                nativeimage.close();
            }
        });
   }
}
