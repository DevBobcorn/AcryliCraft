package com.devbobcorn.acrylic.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import org.lwjgl.system.NativeType;

// Adapted from https://github.com/LemonCaramel/Mica/blob/master/common/src/main/java/moe/caramel/mica/natives/DwmApi.java
public interface DwmApiLib extends Library {

    /* DWM API */
    DwmApiLib INSTANCE = Native.load("dwmapi", DwmApiLib.class);
    int INT_SIZE = 4;

    /* BOOL */
    int BOOL_FALSE = 0;
    int BOOL_TRUE = 1;

    /* DWMWINDOWATTRIBUTE */
    public enum DWM_BOOL_WA {
        DWMWA_USE_IMMERSIVE_DARK_MODE(20);

        public final int key;

        DWM_BOOL_WA(final int key) {
            this.key = key;
        }
    }

    public enum DWM_ENUM_WA {
        DWMWA_WINDOW_CORNER_PREFERENCE(33),
        DWMWA_SYSTEMBACKDROP_TYPE(38);

        public final int key;

        DWM_ENUM_WA(final int key) {
            this.key = key;
        }
    }

    public interface EnumWAValue {
        public int getValue();
    }

    /* DWM_SYSTEMBACKDROP_TYPE */
    public enum DWM_SYSTEMBACKDROP_TYPE implements EnumWAValue {
        DWMSBT_AUTO("auto"), // 0 Auto
        DWMSBT_NONE("none"), // 1 None
        DWMSBT_MAINWINDOW("mica"), // 2 Mica
        DWMSBT_TRANSIENTWINDOW("acrylic"), // 3 Acrylic
        DWMSBT_TABBEDWINDOW("tabbed"); // 4 Tabbed

        public final String translate;

        @Override
        public int getValue() {
            return ordinal();
        }

        DWM_SYSTEMBACKDROP_TYPE(final String translate) {
            this.translate = translate;
        }
    }

    /* DWM_WINDOW_CORNER_PREFERENCE */
    public enum DWM_WINDOW_CORNER_PREFERENCE implements EnumWAValue {
        DWMWCP_DEFAULT("default"), // 0
        DWMWCP_DONOTROUND("do_not_round"), // 1
        DWMWCP_ROUND("round"), // 2
        DWMWCP_ROUNDSMALL("round_small"); // 3

        public final String translate;

        @Override
        public int getValue() {
            return ordinal();
        }

        DWM_WINDOW_CORNER_PREFERENCE(final String translate) {
            this.translate = translate;
        }
    }

    @NativeType("HRESULT") // Err
    int DwmSetWindowAttribute(
        HWND hwnd,
        int dwAttribute,
        PointerType pvAttribute,
        int cbAttribute
    );

    public static void setBooleanWindowAttribute(final long handle, DWM_BOOL_WA attribute, boolean value) {

        final HWND hwnd = new HWND(Pointer.createConstant(handle));

        // DWMWA_USE_IMMERSIVE_DARK_MODE
        INSTANCE.DwmSetWindowAttribute(hwnd, attribute.key, new IntByReference(value ? BOOL_TRUE : BOOL_FALSE), INT_SIZE);
    }

    // "extends" means "implements" here. See https://docs.oracle.com/javase/tutorial/java/generics/bounded.html
    public static <T extends EnumWAValue> void setEnumWindowAttribute(final long handle, DWM_ENUM_WA attribute, T value) {

        final HWND hwnd = new HWND(Pointer.createConstant(handle));

        // DWMWA_USE_IMMERSIVE_DARK_MODE
        INSTANCE.DwmSetWindowAttribute(hwnd, attribute.key, new IntByReference(value.getValue()), INT_SIZE);
    }

}