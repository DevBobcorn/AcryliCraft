package io.devbobcorn.acrylic.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

import java.awt.Color;
import org.lwjgl.system.NativeType;

// Adapted from https://github.com/LemonCaramel/Mica/blob/master/common/src/main/java/moe/caramel/mica/natives/DwmApi.java
public interface DwmApiLib extends Library {

    /* DWM API */
    DwmApiLib INSTANCE = Native.load("dwmapi", DwmApiLib.class);
    int INT_SIZE = 4;

    // DWMWINDOWATTRIBUTE - BOOL ==============================================
    enum DWM_BOOL_WA {
        DWMWA_USE_IMMERSIVE_DARK_MODE(20);

        public final int key;

        DWM_BOOL_WA(final int key) {
            this.key = key;
        }
    }

    int BOOL_FALSE = 0;
    int BOOL_TRUE = 1;

    static void setBoolWA(final long handle, DWM_BOOL_WA attribute, boolean value) {

        final HWND hwnd = new HWND(Pointer.createConstant(handle));

        INSTANCE.DwmSetWindowAttribute(hwnd, attribute.key, new IntByReference(value ? BOOL_TRUE : BOOL_FALSE), INT_SIZE);
    }

    // DWMWINDOWATTRIBUTE - ENUM ==============================================
    enum DWM_ENUM_WA {
        DWMWA_WINDOW_CORNER_PREFERENCE(33),
        DWMWA_SYSTEMBACKDROP_TYPE(38);

        public final int key;

        DWM_ENUM_WA(final int key) {
            this.key = key;
        }
    }

    interface EnumWAValue<T> {
        public int getValue();
        public String getTranslation();
    }

    /* DWM_SYSTEMBACKDROP_TYPE */
    enum DWM_SYSTEMBACKDROP_TYPE implements EnumWAValue<DWM_SYSTEMBACKDROP_TYPE> {
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

        @Override
        public String getTranslation() {
            return translate;
        }

        DWM_SYSTEMBACKDROP_TYPE(final String translate) {
            this.translate = translate;
        }
    }

    /* DWM_WINDOW_CORNER_PREFERENCE */
    enum DWM_WINDOW_CORNER_PREFERENCE implements EnumWAValue<DWM_WINDOW_CORNER_PREFERENCE> {
        DWMWCP_DEFAULT("default"), // 0
        DWMWCP_DONOTROUND("do_not_round"), // 1
        DWMWCP_ROUND("round"), // 2
        DWMWCP_ROUNDSMALL("round_small"); // 3

        public final String translate;

        @Override
        public int getValue() {
            return ordinal();
        }

        @Override
        public String getTranslation() {
            return translate;
        }

        DWM_WINDOW_CORNER_PREFERENCE(final String translate) {
            this.translate = translate;
        }
    }

    // "extends" means "implements" here. See https://docs.oracle.com/javase/tutorial/java/generics/bounded.html
    static <T extends EnumWAValue<T>> void setEnumWA(final long handle, DWM_ENUM_WA attribute, T value) {

        final HWND hwnd = new HWND(Pointer.createConstant(handle));

        INSTANCE.DwmSetWindowAttribute(hwnd, attribute.key, new IntByReference(value.getValue()), INT_SIZE);
    }

    // DWMWINDOWATTRIBUTE - INT ===============================================
    enum DWM_INT_WA {
        DWMWA_BORDER_COLOR(34),
        DWMWA_CAPTION_COLOR(35),
        DWMWA_TEXT_COLOR(36);

        public final int key;

        DWM_INT_WA(final int key) {
            this.key = key;
        }
    }

    // Top 8 bits are not used as alpha channel, but they can be used to
    // specify some special values, e.g. default and none
    // https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/ne-dwmapi-dwmwindowattribute
    int DWMWA_COLOR_DEFAULT = 0xFFFFFFFF; // Int32 -1
    int DWMWA_COLOR_NONE    = 0xFFFFFFFE; // Int32 -2

    Color COLOR_BLACK = new Color(0x000000);
    Color COLOR_WHITE = new Color(0xFFFFFF);

    // Convert RGB to a COLORREF. See https://learn.microsoft.com/en-us/windows/win32/gdi/colorref
    static int rgb2ColorRef(final int rgb) {
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;

        return ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | (r & 0xFF);
    }

    // Convert a COLORREF to RGB. See https://learn.microsoft.com/en-us/windows/win32/gdi/colorref
    static int colorRef2Rgb(final int colorRef, int defaultRgb) {
        if ( (colorRef & 0xFF000000) == 0xFF000000) {
            // Use default value
            return defaultRgb;
        }

        final int b = (colorRef >> 16) & 0xFF;
        final int g = (colorRef >> 8) & 0xFF;
        final int r = colorRef & 0xFF;

        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    static void setIntWA(final long handle, DWM_INT_WA attribute, int value) {

        final HWND hwnd = new HWND(Pointer.createConstant(handle));

        INSTANCE.DwmSetWindowAttribute(hwnd, attribute.key, new IntByReference(value), INT_SIZE);
    }

    @NativeType("HRESULT")
    int DwmSetWindowAttribute(
        HWND hwnd,
        int dwAttribute,
        PointerType pvAttribute,
        int cbAttribute
    );

}