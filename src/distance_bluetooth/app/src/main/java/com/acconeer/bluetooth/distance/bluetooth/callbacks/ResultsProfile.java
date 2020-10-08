package com.acconeer.bluetooth.distance.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.distance.bluetooth.data.RadarResult;

public interface ResultsProfile {
    void onResultChanged(@NonNull final BluetoothDevice device, RadarResult result, boolean isReceived);
}
