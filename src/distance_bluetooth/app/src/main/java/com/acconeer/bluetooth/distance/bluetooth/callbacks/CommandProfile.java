package com.acconeer.bluetooth.distance.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.distance.bluetooth.data.RadarCommand;

public interface CommandProfile {
    void onCommand(@NonNull final BluetoothDevice device, RadarCommand command, boolean isReceived);
}
