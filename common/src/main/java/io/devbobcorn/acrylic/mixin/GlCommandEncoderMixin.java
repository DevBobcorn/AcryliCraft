package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import io.devbobcorn.acrylic.client.rendering.IGlCommandEncoder;
import net.minecraft.util.ARGB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin implements IGlCommandEncoder {

    @Shadow
    private boolean inRenderPass;

    @Final
    @Shadow
    private GlDevice device;

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
            int j = ((GlTexture)colorTexture).getFbo(this.device.directStateAccess(), depthTexture);
            GlStateManager._glBindFramebuffer(36160, j);
            GlStateManager._disableScissorTest();
            GL11.glClearDepth(d);
            GL11.glClearColor(ARGB.redFloat(i), ARGB.greenFloat(i), ARGB.blueFloat(i), ARGB.alphaFloat(i));
            GlStateManager._depthMask(true);
            GlStateManager._colorMask(false, false, false, true);
            GlStateManager._clear(16640);
            GlStateManager._glBindFramebuffer(36160, 0);
            GlStateManager._colorMask(true, true, true, true);
        }
    }
}
