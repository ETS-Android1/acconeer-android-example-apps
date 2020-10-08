package com.acconeer.bluetooth.distance.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

public class Utils {
    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        return adapter != null && adapter.isEnabled();
    }

    public static boolean isLocationEnabled(Context context) {
        try {
            if (Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE) == Settings.Secure.LOCATION_MODE_OFF) {
                return false;
            } else {
                return true;
            }
        } catch (final Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //Thank you StackOverflow
    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
