package com.example.slices.models;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.content.Context;

public class InstanceUtil {
    private static int userID;

    /* https://developer.android.com/reference/android/provider/Settings.Secure
    Source for the ID stuff here - not sure if we will use it 100% but I just wanted to try and see
    how it works. I assume Firestore is the way to go, so this will be removed (probably?)
    -R.P.
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
