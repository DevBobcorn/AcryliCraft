package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(AcrylicMod.MOD_ID)
public class AcrylicModForge {

    public AcrylicModForge(IEventBus bus) {
        bus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLCommonSetupEvent event) {
        /*
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, screen) -> ConfigScreenUtil.create(screen)));

         */
    }
}
