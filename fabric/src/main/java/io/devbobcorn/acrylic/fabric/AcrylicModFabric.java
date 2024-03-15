package io.devbobcorn.acrylic.fabric;

import io.devbobcorn.acrylic.AcrylicMod;
import net.fabricmc.api.ModInitializer;

public class AcrylicModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AcrylicMod.init();
    }
}
