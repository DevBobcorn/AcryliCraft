package io.devbobcorn.acrylic.mixin.fabric;

import io.devbobcorn.acrylic.AcrylicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public ClientLevel level;

    @Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void setScreenHead(Screen newScreen, CallbackInfo callback) {

        AcrylicConfig.getInstance().updateTransparencyStatus(level == null);
    }
}
