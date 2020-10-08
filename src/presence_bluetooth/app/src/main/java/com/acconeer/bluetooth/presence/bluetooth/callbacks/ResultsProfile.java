package com.acconeer.bluetooth.presence.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.presence.model.PresenceResult;

public interface ResultsProfile {
    void onResultChanged(@NonNull final BluetoothDevice device, PresenceResult result, boolean isReceived);
}
