package io.devbobcorn.acrylic.client.rendering;

import java.io.File;
import java.util.function.Consumer;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import org.slf4j.Logger;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;

import net.minecraft.Util;

public class ScreenshotUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void takeScreenshotWithAlpha(RenderTarget rt, Consumer<NativeImage> callback) {
        int w = rt.width;
        int h = rt.height;

        GpuTexture gpuTexture = rt.getColorTexture();
        if (gpuTexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        } else {
            var gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Alpha Screenshot buffer",
                    BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, w * h * gpuTexture.getFormat().pixelSize());
            var commandEncoder = RenderSystem.getDevice().createCommandEncoder();

            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0, () -> {
                try (GpuBuffer.ReadView readView = commandEncoder.readBuffer(gpuBuffer)) {
                    var nativeImage = new NativeImage(w, h, false);

                    for(int y = 0; y < h; ++y) {
                        for(int x = 0; x < w; ++x) {
                            int m = readView.data().getInt((x + y * w) * gpuTexture.getFormat().pixelSize());
                            nativeImage.setPixelABGR(x, h - y - 1, m/* | -16777216*/); // Don't fill alpha channel
                        }
                    }

                    callback.accept(nativeImage);
                }

                gpuBuffer.close();
            }, 0);
        }
    }

    public static void grabWithAlpha(File mcDir, String name, RenderTarget rt) {
        takeScreenshotWithAlpha(rt, nativeimage -> {
            File file1 = new File(mcDir, "screenshots");
            file1.mkdir();

            final File target = new File(file1, name);

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
        });
    }
}
