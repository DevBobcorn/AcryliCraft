package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicMod;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;

@Mod.EventBusSubscriber(modid = AcrylicMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenEventHandler {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {

        var newScreen = event.getNewScreen();

        if (newScreen instanceof TitleScreen) {
            // Preserve mainRT alpha values
            AcrylicMod.setFillMainRTAlpha(false);
        } else {
            // Set alpha of the whole mainRT to 1
            AcrylicMod.setFillMainRTAlpha(true);
        }
    }
}
