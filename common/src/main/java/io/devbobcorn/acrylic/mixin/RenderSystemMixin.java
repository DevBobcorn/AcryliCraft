package io.devbobcorn.acrylic.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.util.TimeSource;

@Mixin(value = RenderSystem.class, remap = false)
public abstract class RenderSystemMixin {

    /**
     * Prefer SDL's native Wayland video driver when the bundled SDL build
     * supports it. Without this, Linux sessions often fall back to the X11
     * driver via XWayland, which breaks transparent-window support for
     * non-OpenGL GPU backends.
     *
     * <p>The hint has to be set before {@code SDL_Init}, so this runs at the
     * head of {@code RenderSystem#initBackendSystem} (Minecraft's SDL
     * initialization entry point).
     */
    @Inject(method = "initBackendSystem", at = @At("HEAD"), remap = false)
    private static void acrylic_mod$preferWaylandDriver(CallbackInfoReturnable<TimeSource.NanoTimeSource> cir) {
        if (Platform.get() != Platform.LINUX) {
            return;
        }

        if (!(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.PREFER_WAYLAND)) {
            return;
        }

        final int driverCount = SDLVideo.SDL_GetNumVideoDrivers();

        for (int i = 0; i < driverCount; i++) {
            if ("wayland".equals(SDLVideo.SDL_GetVideoDriver(i))) {
                SDLHints.SDL_SetHint(SDLHints.SDL_HINT_VIDEO_DRIVER, "wayland");
                return;
            }
        }
    }

}
