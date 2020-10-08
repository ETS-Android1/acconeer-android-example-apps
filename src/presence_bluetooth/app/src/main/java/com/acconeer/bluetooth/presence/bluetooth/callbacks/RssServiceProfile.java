package com.acconeer.bluetooth.presence.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface RssServiceProfile {
    void onRssProfileChanged(@NonNull final BluetoothDevice device, final int value, boolean isReceived);
}
