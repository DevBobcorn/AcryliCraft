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
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void acrylic_mod$renderString(GuiGraphicsExtractor guiGraphicsExtractor, String str, int x, int y) {
        guiGraphicsExtractor.text(s_minecraft.font, str, x, y, ARGB.color(1.0F, -1));
    }

    @Unique
    @SuppressWarnings("null")
    public void acrylic_mod$renderString(GuiGraphicsExtractor guiGraphicsExtractor, Component cp, int x, int y, int color) {
        guiGraphicsExtractor.text(s_minecraft.font, cp, x, y, color);
    }

    @Inject(at = @At("HEAD"), method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
    public void extractRenderStateHead(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();

        } else {
            var config = AcrylicConfig.getInstance();
            var gpuDevice = RenderSystem.getDevice();
            int textPos = 2;

            if ((boolean) config.getValue(AcrylicConfig.SHOW_DEBUG_INFO)) {
                // Draw debug info
                var windowHandle = AcrylicMod.getWindowHandle();

                acrylic_mod$renderString(guiGraphicsExtractor, "Window Handle: " + String.format("0x%016X", windowHandle), 2, textPos);
                textPos += 10;
                var deviceInfo = gpuDevice.getDeviceInfo();
                acrylic_mod$renderString(guiGraphicsExtractor, deviceInfo.name() + " / " +
                        String.format(Locale.ROOT, "%s %s", deviceInfo.backendName(), deviceInfo.driverInfo()), 2, textPos);
                textPos += 10;
            }

            // Check if transparency failed to initialize, and display a hint if this is the case
            if (AcrylicMod.getTransparencyInitFailed()) {
                var hint = translatable(AcrylicMod.getTransparencyInitFailureHintKey());
                acrylic_mod$renderString(guiGraphicsExtractor, hint, 2, textPos, 0xFFFF0000);
            } else if ((boolean) config.getValue(AcrylicConfig.TRANSPARENT_WINDOW) && !AcrylicMod.getTransparencyEnabled()) {
                var hint = translatable(AcrylicMod.MOD_ID + ".hint.restart_for_transparency");
                acrylic_mod$renderString(guiGraphicsExtractor, hint, 2, textPos, 0xFF00FF00);
            }
        }

    }

}
