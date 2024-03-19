package io.devbobcorn.acrylic.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

// Adapted from https://github.com/LemonCaramel/Mica/blob/master/common/src/main/java/moe/caramel/mica/natives/NtDll.java
public interface NtDllLib extends Library {

    NtDllLib INSTANCE = Native.load("ntdll", NtDllLib.class);

    int MINIMUM_MAJOR = 10; // Windows 11 also has a major version of 10
    int MINIMUM_BUILD = 22621;

    /**
     * Check whether current OS is supported.
     *
     * @return True if compatible, otherwise false
     */
    static boolean checkCompatibility() {
        var major = new IntByReference();
        var build = new IntByReference();
        getBuildNumber(major, build);

        return major.getValue() >= MINIMUM_MAJOR && (build.getValue() & 0x0FFFFFFF) >= MINIMUM_BUILD;
    }

    void RtlGetNtVersionNumbers(
        IntByReference MajorVersion,
        IntByReference MinorVersion,
        IntByReference BuildNumber
    );

    static void getBuildNumber(final IntByReference majorVersion, final IntByReference buildNumber) {
        INSTANCE.RtlGetNtVersionNumbers(majorVersion, new IntByReference(), buildNumber);
    }
}