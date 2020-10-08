package com.acconeer.bluetooth.distance.bluetooth.callbacks.data;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.distance.bluetooth.callbacks.ParametersProfile;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarParameters;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import no.nordicsemi.android.ble.data.Data;

public abstract class ParametersDataCallback extends ParsingDataCallback implements ParametersProfile {
    private static final int DATA_SIZE = 20;
    private static final int RANGE_MAX = 7000;
    private static final int RANGE_MIN = 0;
    private static final int GAIN_MIN = 0;
    private static final int GAIN_MAX = 1;
    private static final float UPDATE_RATE_MIN = 0.1f;
    private static final int UPDATE_RATE_MAX = 60;
    private static final int FIXED_THRESHOLD_MIN = RANGE_MIN;
    private static final int FIXED_THRESHOLD_MAX = RANGE_MAX;

    private static final int RANGE_START_OFFSET = 0;
    private static final int RANGE_LENGTH_OFFSET = 4;
    private static final int GAIN_OFFSET = 8;
    private static final int UPDATE_RATE_OFFSET = 12;
    private static final int FIXED_THRESHOLD_OFFSET = 16;

    private static final int M_TO_MM = 1000;

    @Override
    protected void parseData(@NonNull BluetoothDevice device, @NonNull Data data, boolean isReceived) {
        if (data.size() != DATA_SIZE) {
            onInvalidDataReceived(device, data);
        } else {
            FloatBuffer buffer = ByteBuffer.wrap(data.getValue())
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asFloatBuffer();
            float rangeStart = buffer.get() * M_TO_MM;
            float rangeLength = buffer.get() * M_TO_MM;
            float gain = buffer.get();
            float updateRate = buffer.get();
            float fixedThreshold = data.getIntValue(Data.FORMAT_UINT32, FIXED_THRESHOLD_OFFSET);

            Log.d("PDC","Parsed parameters: " + rangeStart + ", " + rangeLength + ", " + gain + ", " +
                    updateRate + ", " + fixedThreshold);
            if (rangeStart < RANGE_MIN || rangeStart > RANGE_MAX) {
                onInvalidDataReceived(device, data);
            } else if (rangeLength < 0 || rangeLength > RANGE_MAX - RANGE_MIN) {
                onInvalidDataReceived(device, data);
            } else if (gain < GAIN_MIN || gain > GAIN_MAX) {
                onInvalidDataReceived(device, data);
            } else if (updateRate < UPDATE_RATE_MIN || updateRate > UPDATE_RATE_MAX) {
                onInvalidDataReceived(device, data);
            } else if (fixedThreshold < FIXED_THRESHOLD_MIN || fixedThreshold > FIXED_THRESHOLD_MAX) {
                onInvalidDataReceived(device, data);
            } else {
                RadarParameters parameters = new RadarParameters(rangeStart, rangeLength, gain,
                        updateRate, fixedThreshold);
                onParametersChanged(device, parameters, isReceived);
            }
        }
    }
}
