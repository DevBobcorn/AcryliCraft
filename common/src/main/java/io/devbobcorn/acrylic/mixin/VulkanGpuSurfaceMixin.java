package io.devbobcorn.acrylic.mixin;

import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import io.devbobcorn.acrylic.AcrylicMod;

// Minecraft 26.2's experimental Vulkan backend always creates its swapchain with
// VkSwapchainCreateInfoKHR.compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR.
//
// VK_COMPOSITE_ALPHA_OPAQUE tells the desktop compositor (e.g. DWM on Windows) to
// ignore the surface's alpha channel and treat the window as fully opaque. As a
// result, even though Acrylic leaves the final swapchain image with alpha == 0 in
// menus (verifiable in RenderDoc), the window is composited as solid black instead
// of being see-through.
//
// The OpenGL backend does not have this problem because GLFW_TRANSPARENT_FRAMEBUFFER
// already configures the GL default framebuffer to be composited with per-pixel
// alpha. To get the same behaviour on Vulkan we must pick a composite-alpha mode
// that respects the alpha channel, restricted to what the surface actually supports
// (creating a swapchain with an unsupported mode would fail).
@Mixin(targets = "com.mojang.blaze3d.vulkan.VulkanGpuSurface")
public class VulkanGpuSurfaceMixin {

    @ModifyArg(
            method = "configure",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;compositeAlpha(I)Lorg/lwjgl/vulkan/VkSwapchainCreateInfoKHR;",
                    remap = false
            ),
            index = 0,
            remap = false
    )
    private int acrylic_mod$transparentCompositeAlpha(int original, @Local VkSurfaceCapabilitiesKHR capabilities) {
        if (!AcrylicMod.getTransparencyEnabled()) {
            return original;
        }

        final int supported = capabilities.supportedCompositeAlpha();

        // Pre-multiplied matches the compositor's expectation on most platforms
        // (and is what GLFW's transparent framebuffer relies on); post-multiplied
        // and inherit are accepted fallbacks. Inherit is what Windows desktop WSI
        // typically exposes alongside opaque, and it inherits the per-pixel alpha
        // setup GLFW already applied to the window.
        if ((supported & KHRSurface.VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR) != 0) {
            return KHRSurface.VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR;
        }

        if ((supported & KHRSurface.VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR) != 0) {
            return KHRSurface.VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR;
        }

        if ((supported & KHRSurface.VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR) != 0) {
            return KHRSurface.VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR;
        }

        // No transparency-capable mode available: keep the backend's original
        // (opaque) choice rather than risk a swapchain creation failure.
        return original;
    }
}
