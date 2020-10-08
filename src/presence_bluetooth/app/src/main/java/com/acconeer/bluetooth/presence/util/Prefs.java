package com.acconeer.bluetooth.presence.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.acconeer.bluetooth.presence.model.RadarParameters;

public class Prefs {
    public static final String RANGE_START_KEY = "range_start";
    public static final String RANGE_LENGTH_KEY = "range_length";
    public static final String UPDATE_RATE_KEY = "update_rate";
    public static final String FIXED_THRESHOLD_KEY = "fixed_threshold";
    public static final String NUM_ZONES_KEY = "num_zones";
    public static final String DISPLAY_DISTANCE_KEY = "display_distance";
    public static final String PROFILE_KEY = "profile";
    public static final String CONNECTED_DEVICE_KEY = "connected_device";
    public static final String START_MEASUREMENT_KEY = "start_measurement";
    public static final String RESTORE_DEFAULTS_KEY = "restore_defaults";

    private static final int DEFAULT_NUM_ZONES = 6;
    private static final boolean DEFAULT_DISPLAY_DISTANCE = false;

    protected static final int RANGE_START_INDEX = 0;
    protected static final int RANGE_LENGTH_INDEX = 1;
    protected static final int UPDATE_RATE_INDEX = 2;
    protected static final int FIXED_THRESHOLD_INDEX = 3;
    protected static final int PROFILE_DEFAULT = 2;

    public static final float[] PROFILE_1_SETTINGS = new float[] {120, 1500, 30, 1.5f};
    public static final float[] PROFILE_2_SETTINGS = new float[] {120, 1500, 30, 1.5f};
    public static final float[] PROFILE_3_SETTINGS = new float[] {180, 1500, 30, 1.5f};
    public static final float[] PROFILE_4_SETTINGS = new float[] {400, 1500, 30, 1.5f};
    public static final float[] PROFILE_5_SETTINGS = new float[] {600, 1500, 30, 1.5f};

    private static final float[][] PROFILE_DEFAULTS = new float[][] {PROFILE_1_SETTINGS,
            PROFILE_2_SETTINGS, PROFILE_3_SETTINGS, PROFILE_4_SETTINGS, PROFILE_5_SETTINGS};

    public static class AllPrefs {
        public RadarParameters parameters;
        public Integer rssProfile;

        public AllPrefs(RadarParameters parameters, Integer rssProfile) {
            this.parameters = parameters;
            this.rssProfile = rssProfile;
        }
    }

    private static float getDefault(float[] defaults, String key, Context context) {
        switch (key) {
            case PROFILE_KEY:
                return PROFILE_DEFAULT - 1;
            case RANGE_START_KEY:
                return defaults[RANGE_START_INDEX];
            case RANGE_LENGTH_KEY:
                return defaults[RANGE_LENGTH_INDEX];
            case UPDATE_RATE_KEY:
                return defaults[UPDATE_RATE_INDEX];
            case FIXED_THRESHOLD_KEY:
                return defaults[FIXED_THRESHOLD_INDEX];
            default:
                throw new IllegalArgumentException("No pref with key " + key + " found!");
        }
    }

    public static boolean isDistanceDisplayed(Context context) {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);

        return manager.getBoolean(DISPLAY_DISTANCE_KEY, DEFAULT_DISPLAY_DISTANCE);
    }

    public static int getNumZones(Context context) {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);

        return Integer.parseInt(manager.getString(NUM_ZONES_KEY, String.valueOf(DEFAULT_NUM_ZONES)));
    }

    public static float getDefault(String key, Context context) {
        int profile = getProfile(context);
        float[] defaults = PROFILE_DEFAULTS[profile - 1];

        return getDefault(defaults, key, context);
    }

    public static float getPref(String key, Context context) {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);
        int profile = getProfile(context);
        float[] defaults = PROFILE_DEFAULTS[profile - 1];

        return Float.parseFloat(manager.getString(key, String.valueOf(getDefault(defaults, key, context))));
    }

    public static AllPrefs getPrefs(Context context) {
        return new AllPrefs(getParams(context), getProfile(context));
    }

    public static Integer getProfile(Context context) {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);

        return manager.getInt(PROFILE_KEY, PROFILE_DEFAULT - 1) + 1;
    }

    public static RadarParameters getParams(Context context) {
        float rangeStart = getPref(RANGE_START_KEY, context);
        float rangeLength = getPref(RANGE_LENGTH_KEY, context);
        float updateRate = getPref(UPDATE_RATE_KEY, context);
        float fixedThreshold = getPref(FIXED_THRESHOLD_KEY, context);

        return new RadarParameters(rangeStart, rangeLength, updateRate, fixedThreshold);
    }
}
