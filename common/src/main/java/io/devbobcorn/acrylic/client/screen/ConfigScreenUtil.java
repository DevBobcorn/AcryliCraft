package io.devbobcorn.acrylic.client.screen;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;

import io.devbobcorn.acrylic.nativelib.NtDllLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import static net.minecraft.network.chat.Component.translatable;

import java.awt.Color;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.nativelib.DwmApiLib;
import io.devbobcorn.acrylic.nativelib.DwmApiLib.EnumWAValue;

/**
 * Mod Config screen helper
 */
public final class ConfigScreenUtil {

    /**
     * Create a config screen.
     *
     * @param screen previous screen
     * @return config screen
     */
    @SuppressWarnings("null")
    public static Screen createIfCompatible(final Screen screen) {

        // Check OS compatibility
        if (!NtDllLib.checkCompatibility()) {
            return new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(screen),
                    translatable("acrylic.unsupported.title").withStyle(ChatFormatting.BOLD, ChatFormatting.RED),
                    translatable("acrylic.unsupported.description")
            );
        }

        // Create Mod Config screen
        return create(screen);
    }

    public static Screen create(final Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(translatable("acrylic.mod_name"))
                .category(categoryGeneral())
                .build().generateScreen(parent);
    }

    private static Option<Boolean> boolOption(String key, boolean defValue) {
        return Option.<Boolean>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
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

    private static Option<Color> colorOption(String key, Color defValue) {
        return Option.<Color>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
                .controller(ColorControllerBuilder::create)
                .binding(
                        defValue,
                        () -> new Color( (int) AcrylicConfig.getInstance().getValue(key) ),
                        value -> {
                            AcrylicConfig.getInstance().setValue(key, value.getRGB());
                        }
                )
                .instant(true)
                .build();
    }

    private static <T extends Enum<T>> Option<T> enumOption(String key, Class<T> enumClass, T defValue) {
        return Option.<T>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
                .controller(option -> EnumControllerBuilder.create((Option<T>) option)
                        .enumClass(enumClass)
                        .valueFormatter(type -> {
                            @SuppressWarnings("unchecked")
                            var t = (EnumWAValue<T>) type;
                            return translatable(AcrylicMod.MOD_ID + ".config." + key + ".type." + t.getTranslation());
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

                .group(OptionGroup.createBuilder()
                        .name(translatable(AcrylicMod.MOD_ID + ".config.window"))

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

                        .build()
                )

                // Window Border Customization
                .group(OptionGroup.createBuilder()
                        .name(translatable(AcrylicMod.MOD_ID + ".config.border"))

                        .option( boolOption(AcrylicConfig.HIDE_BORDER, false) )
                        .option( boolOption(AcrylicConfig.CUSTOMIZE_BORDER, false) )
                        .option( colorOption(AcrylicConfig.BORDER_COLOR, DwmApiLib.COLOR_BLACK) )

                        .build()
                )

                // Show debug info
                .option( boolOption(AcrylicConfig.SHOW_DEBUG_INFO, false) )

                .build();
    }

}
