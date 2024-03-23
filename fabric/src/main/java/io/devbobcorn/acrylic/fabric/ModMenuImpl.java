package io.devbobcorn.acrylic.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.devbobcorn.acrylic.client.screen.ConfigScreenUtil;
import net.minecraft.client.gui.screens.Screen;

public final class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<? extends Screen> getModConfigScreenFactory() {
        return ConfigScreenUtil::create;
    }
}
