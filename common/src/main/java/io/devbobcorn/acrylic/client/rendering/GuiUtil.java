package io.devbobcorn.acrylic.client.rendering;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public class GuiUtil {

    @SuppressWarnings("null")
    public static void fillGradient(Matrix4f pose, BufferBuilder builder, int x0, int y0, int x1, int y1, int z, int color0, int color1) {
        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;
        builder.addVertex(pose, (float)x1, (float)y0, (float)z).setColor(r0, g0, b0, a0);
        builder.addVertex(pose, (float)x0, (float)y0, (float)z).setColor(r0, g0, b0, a0);
        builder.addVertex(pose, (float)x0, (float)y1, (float)z).setColor(r1, g1, b1, a1);
        builder.addVertex(pose, (float)x1, (float)y1, (float)z).setColor(r1, g1, b1, a1);
    }
}
