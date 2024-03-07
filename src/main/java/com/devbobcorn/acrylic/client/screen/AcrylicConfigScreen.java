package com.devbobcorn.acrylic.client.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import static net.minecraft.network.chat.Component.translatable;

import com.devbobcorn.acrylic.AcrylicConfig;
import com.devbobcorn.acrylic.AcrylicMod;
import com.devbobcorn.acrylic.nativelib.DwmApiLib;
import com.devbobcorn.acrylic.nativelib.DwmApiLib.EnumWAValue;

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

    private static Option<Boolean> boolOption(String key, boolean defValue) {
        return Option.<Boolean>createBuilder()
                .name(translatable(AcrylicMod.MODID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MODID + ".config." + key + ".description")))
                .controller(BooleanControllerBuilder::create)
                .binding(
                    defValue,
                    () -> AcrylicConfig.getInstance().getValue(key),
                    value -> {
                        AcrylicConfig.getInstance().setValue(key, value);
                    }
                )
                .instant(true)
                .build();
    }

    private static <T extends Enum<T>> Option<T> enumOption(String key, Class<T> enumClass, T defValue) {
        return Option.<T>createBuilder()
                .name(translatable(AcrylicMod.MODID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MODID + ".config." + key + ".description")))
                .controller(option -> EnumControllerBuilder.create((Option<T>) option)
                    .enumClass(enumClass)
                    .valueFormatter(type -> {
                        @SuppressWarnings("unchecked")
                        var t = (EnumWAValue<T>) type;
                        return translatable(AcrylicMod.MODID + ".config." + key + ".type." + t.getTranslation());
                    })
                )
                .binding(
                    defValue,
                    () -> AcrylicConfig.getInstance().getValue(key),
                    value -> {
                        AcrylicConfig.getInstance().setValue(key, value);
                    }
                )
                .instant(true)
                .build();
    }

    private static ConfigCategory categoryGeneral() {
        return ConfigCategory.createBuilder()
            .name(translatable("acrylic.config"))

            // Use Immersive Dark Mode
            .option( boolOption(AcrylicConfig.USE_IMMERSIVE_DARK_MODE, false) )

            // System Backdrop Type
            .option( enumOption(AcrylicConfig.SYSTEM_BACKDROP_TYPE,
                        DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.class,
                        DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO)
            )

            // Window Corner Preference
            .option( enumOption(AcrylicConfig.WINDOW_CORNER_PREFERENCE,
                        DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.class,
                        DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT)
            )
            
            .build();
    }

}