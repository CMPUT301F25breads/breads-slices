package com.example.slices.testing;

/**
 * Utility class for debug logging that supports Android and non-Android environments
 *
 */
public class DebugLogger {
    /**
     * Indicates whether the Android Log class is available
     */
    private static final boolean HAS_ANDROID_LOG;

    // Static initializer to determine if Android Log is available
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

    /**
     * Logs a debug message
     * <p>
     * If running on Android, it uses Log.d() via reflection.
     * Otherwise, prints to standard output.
     * </p>
     *
     * @param tag
     *      Tag for the debug message (usually the class name)
     * @param msg
     *      Debug message content
     */
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
