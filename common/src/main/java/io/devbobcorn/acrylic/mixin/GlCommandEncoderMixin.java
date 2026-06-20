package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.FrameBufferAttachment;
import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.util.ARGB;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin implements IGlCommandEncoder {

    @Unique
    private static Field acrylic_mod$deviceField;

    @Unique
    private static Method acrylic_mod$dsaMethod;

    @Unique
    private static Method acrylic_mod$frameBufferCacheMethod;

    @Unique
    private static boolean acrylic_mod$reflectionInitialized;

    @Override
    public void acrylic_mod$fillColorAlphaAndDepth(GpuTexture colorTexture, int i, GpuTexture depthTexture, double d) {
        if (!colorTexture.getFormat().hasColorAspect()) {
            throw new IllegalStateException("Trying to clear a non-color texture as color");
        } else if (!depthTexture.getFormat().hasDepthAspect()) {
            throw new IllegalStateException("Trying to clear a non-depth texture as depth");
        } else if (colorTexture.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        } else if (depthTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
        } else {
            try {
                if (!acrylic_mod$reflectionInitialized) {
                    acrylic_mod$deviceField = this.getClass().getDeclaredField("device");
                    acrylic_mod$deviceField.setAccessible(true);
                    // GlDevice is package-private, so its accessors are resolved reflectively.
                    acrylic_mod$dsaMethod = acrylic_mod$deviceField.getType().getMethod("directStateAccess");
                    acrylic_mod$frameBufferCacheMethod = acrylic_mod$deviceField.getType().getMethod("frameBufferCache");
                    acrylic_mod$reflectionInitialized = true;
                }

                Object device = acrylic_mod$deviceField.get(this);
                DirectStateAccess dsa = (DirectStateAccess) acrylic_mod$dsaMethod.invoke(device);
                FrameBufferCache frameBufferCache = (FrameBufferCache) acrylic_mod$frameBufferCacheMethod.invoke(device);

                // Minecraft 26.2 moved FBO management off GlTexture and onto a shared
                // FrameBufferCache keyed by the color attachment list and depth attachment.
                int j = frameBufferCache.getFbo(dsa, List.of((FrameBufferAttachment) colorTexture), (FrameBufferAttachment) depthTexture);
                GlStateManager._glBindFramebuffer(36160, j);
                GlStateManager._disableScissorTest();
                GL11.glClearDepth(d);
                GL11.glClearColor(ARGB.redFloat(i), ARGB.greenFloat(i), ARGB.blueFloat(i), ARGB.alphaFloat(i));
                GlStateManager._depthMask(true);
                GlStateManager._colorMask(8);
                GlStateManager._clear(16640);
                GlStateManager._glBindFramebuffer(36160, 0);
                GlStateManager._colorMask(15);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access GlDevice.directStateAccess()", e);
            }
        }
    }
}
