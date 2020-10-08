package com.acconeer.bluetooth.distance.bluetooth.callbacks.data;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class ParsingDataCallback implements ProfileDataCallback, DataSentCallback {
    @Override
    public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
        Log.d("DataCallback", "Sent data: " + data);
        parseData(device, data, false);
    }

    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        Log.d("DataCallback", "Received data: " + data);
        parseData(device, data, true);
    }

    @Override
    public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        Log.d("DataCallback", "Failed parsing data: " + data.toString());
    }

    protected abstract void parseData(@NonNull BluetoothDevice device, @NonNull Data data, boolean isReceive);
}
