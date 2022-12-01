package com.sajh1s2.espblufi.saj;

import android.util.Log;

/* loaded from: classes3.dex */
public final class AppLog {
    private static String defaultTag = "appLog";
    public static boolean isPrint = true;

    public static void d(String str, String str2) {
        if (!isPrint || str2 == null) {
            return;
        }
        Log.d(str, str2);
    }

    public static void d(String str) {
        if (!isPrint || str == null) {
            return;
        }
        Log.d(defaultTag, str);
    }

    public static void i(String str) {
        if (!isPrint || str == null) {
            return;
        }
        Log.i(defaultTag, str);
    }

    public static void w(String str) {
        if (!isPrint || str == null) {
            return;
        }
        Log.w(defaultTag, str);
    }

    public static void e(String str) {
        if (!isPrint || str == null) {
            return;
        }
        Log.e(defaultTag, str);
    }
}