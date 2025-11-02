package com.example.slices.testing;

public class DebugLogger {
    private static final boolean HAS_ANDROID_LOG;

    static {
        boolean hasAndroid;
        try {
            Class.forName("android.util.Log");
            hasAndroid = true;
        } catch (ClassNotFoundException e) {
            hasAndroid = false;
        }
        HAS_ANDROID_LOG = hasAndroid;
    }

    public static void d(String tag, String msg) {
        if (HAS_ANDROID_LOG) {
            try {
                // Use reflection so we don't statically reference Log.d()
                Class<?> logClass = Class.forName("android.util.Log");
                logClass.getMethod("d", String.class, String.class)
                        .invoke(null, tag, msg);
            } catch (Exception e) {
                System.out.println("[DEBUG] " + tag + ": " + msg);
            }
        } else {
            System.out.println("[DEBUG] " + tag + ": " + msg);
        }
    }
}
