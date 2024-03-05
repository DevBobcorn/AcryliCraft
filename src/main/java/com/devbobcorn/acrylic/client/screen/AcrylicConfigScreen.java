package com.devbobcorn.acrylic.client.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.minecraft.network.chat.Component.translatable;

import com.devbobcorn.acrylic.AcrylicConfig;
import com.devbobcorn.acrylic.nativelib.DwmApiLib;

/**
 * Mod Config screen
 */
public final class AcrylicConfigScreen {

    public static Screen create(final Screen screen) {
        return YetAnotherConfigLib.createBuilder()
            .title(translatable("acrylic.mod_name"))
            .category(categoryGeneral())
            .build().generateScreen(screen);
    }

    private static ConfigCategory categoryGeneral() {
        return ConfigCategory.createBuilder()
            .name(translatable("acrylic.config"))

            // Use Immersive Dark Mode
            .option(Option.<Boolean>createBuilder()
                .name(translatable("acrylic.config.use_immersive_dark_mode"))
                .description(OptionDescription.of(translatable("acrylic.config.use_immersive_dark_mode.description")))
                .controller(BooleanControllerBuilder::create)
                .binding(
                    false,
                    () -> AcrylicConfig.getInstance().useImmersiveDarkMode.get(),
                    value -> {
                        final AcrylicConfig config = AcrylicConfig.getInstance();
                        config.setConfig(config.useImmersiveDarkMode, value);
                    }
                )
                .instant(true)
                .build()
            )

            // System Backdrop Type
            .option(Option.<DwmApiLib.DWM_SYSTEMBACKDROP_TYPE>createBuilder()
                .name(translatable("acrylic.config.system_backdrop_type"))
                .description(OptionDescription.of(
                    translatable("acrylic.config.system_backdrop_type.description")
                ))
                .controller(option -> EnumControllerBuilder.create(option).enumClass(DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.class).valueFormatter(type -> {
                    return translatable("acrylic.config.system_backdrop_type.type." + type.translate);
                }))
                .binding(
                    DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO,
                    () -> AcrylicConfig.getInstance().systemBackdropType.get(),
                    value -> {
                        final AcrylicConfig config = AcrylicConfig.getInstance();
                        config.setConfig(config.systemBackdropType, value);
                    }
                )
                .instant(true)
                .build()
            )

            // Window Corner Preference
            .option(Option.<DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE>createBuilder()
                .name(translatable("acrylic.config.corner_preference"))
                .description(OptionDescription.of(
                    translatable("acrylic.config.corner_preference.description")
                ))
                .controller(option -> EnumControllerBuilder.create(option).enumClass(DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.class).valueFormatter(type -> {
                    return translatable("acrylic.config.corner_preference.type." + type.translate);
                }))
                .binding(
                    DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT,
                    () -> AcrylicConfig.getInstance().windowCorner.get(),
                    value -> {
                        final AcrylicConfig config = AcrylicConfig.getInstance();
                        config.setConfig(config.windowCorner, value);
                    }
                )
                .instant(true)
                .build()
            )
            
            .build();
    }

}