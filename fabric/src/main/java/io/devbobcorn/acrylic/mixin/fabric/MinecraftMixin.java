package io.devbobcorn.acrylic.mixin.fabric;

import io.devbobcorn.acrylic.AcrylicMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void setScreenHead(Screen newScreen, CallbackInfo callback) {

        if (newScreen instanceof TitleScreen) {
            // Preserve mainRT alpha values
            AcrylicMod.setFillMainRTAlpha(false);
        } else {
            // Set alpha of the whole mainRT to 1
            AcrylicMod.setFillMainRTAlpha(true);
        }
    }
}
