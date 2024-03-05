package com.devbobcorn.acrylic.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

// Adapted from https://github.com/LemonCaramel/Mica/blob/master/common/src/main/java/moe/caramel/mica/natives/NtDll.java
public interface NtDllLib extends Library {

    NtDllLib INSTANCE = Native.load("ntdll", NtDllLib.class);

    void RtlGetNtVersionNumbers(
        IntByReference MajorVersion,
        IntByReference MinorVersion,
        IntByReference BuildNumber
    );

    static void getBuildNumber(final IntByReference majorVersion, final IntByReference buildNumber) {
        INSTANCE.RtlGetNtVersionNumbers(majorVersion, new IntByReference(), buildNumber);
    }
}