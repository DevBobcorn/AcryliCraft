package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.client.screen.ConfigScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(AcrylicMod.MOD_ID)
public class AcrylicModForge {

    public AcrylicModForge(IEventBus bus) {
        bus.addListener(this::clientSetup);
    }

    class AcrylicConfigScreenFactory implements IConfigScreenFactory {

        @Override
        public Screen createScreen(Minecraft mc, Screen screen) {
            return ConfigScreenUtil.create(screen);
        }
    }

    private void clientSetup(final FMLCommonSetupEvent event) {

        ModLoadingContext.get().registerExtensionPoint(AcrylicConfigScreenFactory.class,
                AcrylicConfigScreenFactory::new);

    }
}
