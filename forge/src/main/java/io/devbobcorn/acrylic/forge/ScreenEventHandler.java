package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicMod;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = AcrylicMod.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
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
