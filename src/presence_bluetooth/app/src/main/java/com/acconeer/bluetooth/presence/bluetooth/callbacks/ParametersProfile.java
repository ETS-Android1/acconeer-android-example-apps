package com.acconeer.bluetooth.presence.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.presence.model.RadarParameters;

public interface ParametersProfile {
    void onParametersChanged(@NonNull final BluetoothDevice device, RadarParameters parameters, boolean isReceived);
}
