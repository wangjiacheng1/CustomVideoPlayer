package com.org.customvideoplayer.utils;

import android.os.Build;

public class DeviceUtils {

    public static boolean checkAndroid_Q(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
