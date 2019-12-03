package com.tencent.tvs.cloudapi.tools;

import android.util.Log;

/**
 * Created by sapphireqin on 2019/11/28.
 */

public class Logger {
    private static final String HEADER = "sap.";

    public static void e(String tag, String text) {
        Log.e(HEADER + tag, text);
    }

    public static void e(String tag, String text, Throwable e) {
        Log.e(HEADER + tag, text, e);
    }

    public static void w(String tag, String text) {
        Log.w(HEADER + tag, text);
    }

    public static void i(String tag, String text) {
        Log.w(HEADER + tag, text);
    }
}
