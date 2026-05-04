package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import net.minecraft.util.ARGB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin implements IGlCommandEncoder {

    @Shadow
    private boolean inRenderPass;

    @Override
    public void acrylic_mod$fillColorAlphaAndDepth(GpuTexture colorTexture, int i, GpuTexture depthTexture, double d) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        } else if (!colorTexture.getFormat().hasColorAspect()) {
            throw new IllegalStateException("Trying to clear a non-color texture as color");
        } else if (!depthTexture.getFormat().hasDepthAspect()) {
            throw new IllegalStateException("Trying to clear a non-depth texture as depth");
        } else if (colorTexture.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        } else if (depthTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
        } else {
            try {
                Field deviceField = this.getClass().getDeclaredField("device");
                deviceField.setAccessible(true);
                Object device = deviceField.get(this);
                Method dsaMethod = device.getClass().getMethod("directStateAccess");
                DirectStateAccess dsa = (DirectStateAccess) dsaMethod.invoke(device);

                int j = ((GlTexture)colorTexture).getFbo(dsa, depthTexture);
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
