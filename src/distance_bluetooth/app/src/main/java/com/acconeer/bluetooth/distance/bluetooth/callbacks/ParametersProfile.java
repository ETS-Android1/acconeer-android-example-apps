package com.acconeer.bluetooth.distance.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.distance.bluetooth.data.RadarParameters;

public interface ParametersProfile {
    void onParametersChanged(@NonNull final BluetoothDevice device, RadarParameters parameters, boolean isReceived);
}
