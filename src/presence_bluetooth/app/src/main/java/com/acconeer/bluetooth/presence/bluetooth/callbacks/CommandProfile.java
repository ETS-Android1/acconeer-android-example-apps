package com.acconeer.bluetooth.presence.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.presence.model.RadarCommand;

public interface CommandProfile {
    void onCommand(@NonNull final BluetoothDevice device, RadarCommand command, boolean isReceived);
}
