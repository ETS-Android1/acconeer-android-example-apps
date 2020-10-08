package com.acconeer.bluetooth.presence.bluetooth.callbacks.data;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.presence.bluetooth.callbacks.CommandProfile;
import com.acconeer.bluetooth.presence.model.RadarCommand;

import no.nordicsemi.android.ble.data.Data;

public abstract class CommandDataCallback extends ParsingDataCallback implements CommandProfile {
    private static final int DATA_SIZE = 1;

    @Override
    protected void parseData(@NonNull BluetoothDevice device, @NonNull Data data, boolean isReceived) {
        if (data.size() != DATA_SIZE) {
            onInvalidDataReceived(device, data);
        } else {
            onCommand(device, RadarCommand.fromOrdinal(data.getByte(0)), isReceived);
        }
    }
}
