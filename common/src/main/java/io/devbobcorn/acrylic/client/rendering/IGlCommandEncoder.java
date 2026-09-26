package io.devbobcorn.acrylic.client.rendering;

import com.mojang.renderpearl.api.textures.GpuTexture;

public interface IGlCommandEncoder {

    public void acrylic_mod$fillColorAlphaAndDepth(GpuTexture colorTexture, int i, GpuTexture depthTexture, double d);
}
