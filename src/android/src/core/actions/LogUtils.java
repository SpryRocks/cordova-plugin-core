package com.cordova.core.actions;

import android.util.Log;

public class LogUtils {
    public static void debug(String msg) {
        log(Log.INFO, msg);
    }

    public static void log(int priority, String msg) {
        Log.println(priority, "actions.core", msg);
    }
}
