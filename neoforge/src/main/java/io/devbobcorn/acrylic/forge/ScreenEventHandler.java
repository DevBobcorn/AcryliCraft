package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = AcrylicMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenEventHandler {

    private static Minecraft s_minecraft;

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {

        if (s_minecraft == null) {
            s_minecraft = Minecraft.getInstance();
        }

        AcrylicConfig.getInstance().updateTransparencyStatus(s_minecraft.level == null);
    }
}
