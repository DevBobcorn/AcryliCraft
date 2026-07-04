package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.platform.GLX;
import io.devbobcorn.acrylic.AcrylicConfig;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.LongSupplier;

@Mixin(value = GLX.class, remap = false)
public abstract class GLXMixin {

    /**
     * Prefer GLFW's native Wayland backend (393219 / 0x00060003) when the bundled
     * library supports it. Without this, Linux sessions often fall back to the X11
     * backend via XWayland, which breaks transparent-framebuffer support for
     * non-OpenGL GPU backends.
     */
    @Inject(
            method = "_initGlfw",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwInit()Z",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private static void acrylic_mod$preferWaylandPlatform(CallbackInfoReturnable<LongSupplier> cir) {
        if (Platform.get() != Platform.LINUX) {
            return;
        }

        if (!(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.PREFER_WAYLAND)) {
            return;
        }

        if (GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_WAYLAND)) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
        }
    }

}
