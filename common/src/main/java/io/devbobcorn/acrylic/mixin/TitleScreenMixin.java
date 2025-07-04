package io.devbobcorn.acrylic.mixin;

import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static net.minecraft.network.chat.Component.translatable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private Minecraft s_minecraft = null;

    @Unique
    @SuppressWarnings("null")
    public void acrylic_mod$renderString(GuiGraphics guiGraphics, String str, int x, int y) {
        guiGraphics.drawString(s_minecraft.font, str, x, y, ARGB.color(1.0F, -1));
    }

    @Unique
    @SuppressWarnings("null")
    public void acrylic_mod$renderString(GuiGraphics guiGraphics, Component cp, int x, int y, int color) {
        guiGraphics.drawString(s_minecraft.font, cp, x, y, color);
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    public void renderHead(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();

        } else {
            var config = AcrylicConfig.getInstance();
            var gpuDevice = RenderSystem.getDevice();
            int textPos = 2;

            if ((boolean) config.getValue(AcrylicConfig.SHOW_DEBUG_INFO)) {
                // Draw debug info
                var windowHandle = AcrylicMod.getWindowHandle();

                acrylic_mod$renderString(guiGraphics, "Window Handle: " + String.format("0x%016X", windowHandle), 2, textPos);
                textPos += 10;
                acrylic_mod$renderString(guiGraphics, gpuDevice.getRenderer() + " / " +
                        String.format(Locale.ROOT, "%s %s", gpuDevice.getBackendName(), gpuDevice.getVersion()), 2, textPos);
                textPos += 10;
            }

            // Check if transparency failed to initialize, and display a hint if this is the case
            if (AcrylicMod.getTransparencyInitFailed()) {
                var hint = translatable(AcrylicMod.MOD_ID + ".hint.transparency_init_failure");
                acrylic_mod$renderString(guiGraphics, hint, 2, textPos, 0xFFFF0000);
            } else if ((boolean) config.getValue(AcrylicConfig.TRANSPARENT_WINDOW) && !AcrylicMod.getTransparencyEnabled()) {
                var hint = translatable(AcrylicMod.MOD_ID + ".hint.restart_for_transparency");
                acrylic_mod$renderString(guiGraphics, hint, 2, textPos, 0xFF00FF00);
            }
        }

    }

}
