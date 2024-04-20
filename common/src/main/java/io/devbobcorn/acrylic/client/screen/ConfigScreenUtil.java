package io.devbobcorn.acrylic.client.screen;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import static net.minecraft.network.chat.Component.translatable;

import java.awt.Color;
import java.util.function.Consumer;

import io.devbobcorn.acrylic.AcrylicConfig;
import io.devbobcorn.acrylic.AcrylicMod;
import io.devbobcorn.acrylic.nativelib.NtDllLib;
import io.devbobcorn.acrylic.nativelib.DwmApiLib;
import io.devbobcorn.acrylic.nativelib.DwmApiLib.EnumWAValue;

/**
 * Mod Config screen helper
 */
public final class ConfigScreenUtil {

    /**
     * Create a config screen.
     *
     * @param parent previous screen
     * @return config screen
     */
    public static Screen create(final Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(translatable("acrylic.mod_name"))

                .category(categoryGeneral())
                .category(categoryWin11Specific())

                .build().generateScreen(parent);
    }

    private static Option<Boolean> boolOption(String key, boolean defValue, boolean available, Consumer<Boolean> valueCallback) {
        return Option.<Boolean>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
                .controller(BooleanControllerBuilder::create)
                .binding(
                        defValue,
                        () -> AcrylicConfig.getInstance().getValue(key),
                        value -> {
                            AcrylicConfig.getInstance().setValue(key, value);
                            valueCallback.accept(value);
                        }
                )
                .instant(true)
                .available(available)
                .build();
    }

    private static LabelOption labelOption(String key) {
        return LabelOption.createBuilder()
                .line(translatable(AcrylicMod.MOD_ID + ".config." + key))

                .build();
    }

    private static Option<Color> colorOption(String key, Color defValue, boolean available, Consumer<Color> valueCallback) {
        return Option.<Color>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
                .controller(ColorControllerBuilder::create)
                .binding(
                        defValue,
                        () -> new Color( AcrylicConfig.getInstance().getValue(key) ),
                        value -> {
                            AcrylicConfig.getInstance().setValue(key, value.getRGB());
                            valueCallback.accept(value);
                        }
                )
                .instant(true)
                .available(available)
                .build();
    }

    private static <T extends Enum<T>> Option<T> enumOption(String key, Class<T> enumClass, T defValue, boolean available, Consumer<T> valueCallback) {

        return Option.<T>createBuilder()
                .name(translatable(AcrylicMod.MOD_ID + ".config." + key))
                .description(OptionDescription.of(translatable(AcrylicMod.MOD_ID + ".config." + key + ".description")))
                .controller(option -> EnumControllerBuilder.create(option)
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
                            valueCallback.accept(value);
                        }
                )
                .instant(true)
                .available(available)
                .build();
    }

    private static ConfigCategory categoryGeneral() {

        return ConfigCategory.createBuilder()
                .name(translatable("acrylic.config.general"))

                // Show debug info
                .option( boolOption(AcrylicConfig.SHOW_DEBUG_INFO, false, true, (val) -> { }) )

                // Transparent window
                .option( boolOption(AcrylicConfig.TRANSPARENT_WINDOW, true, true, (val) -> { }) )

                .build();
    }

    private static ConfigCategory categoryWin11Specific() {

        // Check OS compatibility
        if (!NtDllLib.checkCompatibility()) {

            return ConfigCategory.createBuilder()
                    .name(translatable("acrylic.config.win11_specific"))

                    .group(OptionGroup.createBuilder()
                            .name(translatable("acrylic.config.unsupported")
                                    .withStyle(ChatFormatting.BOLD, ChatFormatting.RED))

                            .option( labelOption("unsupported.win11_only") )

                            .build()
                    ).build();
        }

        final Option<Boolean> useImmersiveDarkModeOption = boolOption(AcrylicConfig.USE_IMMERSIVE_DARK_MODE, true,
                !(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.SYNC_WITH_OS_THEME), (val) -> { });

        final Option<Color> borderColorOption = colorOption(AcrylicConfig.BORDER_COLOR, DwmApiLib.COLOR_BLACK,
                AcrylicConfig.getInstance().getValue(AcrylicConfig.CUSTOMIZE_BORDER), (val) -> { });

        final Option<Boolean> customBorderOption = boolOption(AcrylicConfig.CUSTOMIZE_BORDER, false,
                !(boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.HIDE_BORDER),
                borderColorOption::setAvailable);

        return ConfigCategory.createBuilder()
                .name(translatable("acrylic.config.win11_specific"))

                .group(OptionGroup.createBuilder()
                        .name(translatable(AcrylicMod.MOD_ID + ".config.window"))

                        // Sync with OS Theme
                        .option( boolOption(AcrylicConfig.SYNC_WITH_OS_THEME, true, true, (val) -> {
                                    useImmersiveDarkModeOption.setAvailable(!val);
                                }) )

                        // Use Immersive Dark Mode
                        .option( useImmersiveDarkModeOption )

                        // System Backdrop Type
                        .option( enumOption(AcrylicConfig.SYSTEM_BACKDROP_TYPE,
                                DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.class,
                                DwmApiLib.DWM_SYSTEMBACKDROP_TYPE.DWMSBT_AUTO,
                                true,
                                (val) -> { })
                        )

                        // Window Corner Preference
                        .option( enumOption(AcrylicConfig.WINDOW_CORNER_PREFERENCE,
                                DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.class,
                                DwmApiLib.DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_DEFAULT,
                                true,
                                (val) -> { })
                        )

                        .build()
                )

                // Window Border Customization
                .group(OptionGroup.createBuilder()
                        .name(translatable(AcrylicMod.MOD_ID + ".config.border"))

                        .option( boolOption(AcrylicConfig.HIDE_BORDER, false, true, (val) -> {
                                    customBorderOption.setAvailable(!val);
                                    borderColorOption.setAvailable(!val && (boolean) AcrylicConfig.getInstance().getValue(AcrylicConfig.CUSTOMIZE_BORDER));
                                }) )
                        .option( customBorderOption )
                        .option( borderColorOption )

                        .build()
                )

                .build();
    }

}
