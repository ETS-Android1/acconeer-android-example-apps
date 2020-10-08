package com.acconeer.bluetooth.distance.bluetooth.callbacks.data;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.distance.bluetooth.callbacks.RssServiceProfile;

import no.nordicsemi.android.ble.data.Data;

public abstract class RssServiceDataCallback extends ParsingDataCallback implements RssServiceProfile {
    private static final int MIN_PROFILE = 1;
    private static final int MAX_PROFILE = 5;
    private static final int DATA_SIZE = 1;

    @Override
    protected void parseData(@NonNull BluetoothDevice device, @NonNull Data data, boolean isReceived) {
        if (data.size() != DATA_SIZE) {
            onInvalidDataReceived(device, data);
        } else {
            int profile = data.getIntValue(Data.FORMAT_UINT8, 0);
            if (profile < MIN_PROFILE || profile > MAX_PROFILE) {
                onInvalidDataReceived(device, data);
            } else {
                onRssProfileChanged(device, profile, isReceived);
            }
        }
    }
}
