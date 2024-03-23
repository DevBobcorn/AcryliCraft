package io.devbobcorn.acrylic.forge;

import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.client.screen.ConfigScreenUtil;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AcrylicMod.MOD_ID)
public class AcrylicModForge {

    public AcrylicModForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final FMLCommonSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, screen) -> ConfigScreenUtil.create(screen)));
    }
}
