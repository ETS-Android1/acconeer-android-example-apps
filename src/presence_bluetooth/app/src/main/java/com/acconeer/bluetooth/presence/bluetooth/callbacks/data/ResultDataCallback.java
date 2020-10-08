package com.acconeer.bluetooth.presence.bluetooth.callbacks.data;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.acconeer.bluetooth.presence.bluetooth.callbacks.ResultsProfile;
import com.acconeer.bluetooth.presence.model.PresenceResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import no.nordicsemi.android.ble.data.Data;

public abstract class ResultDataCallback extends ParsingDataCallback implements ResultsProfile {
    private static final int DATA_SIZE = 12;

    private static final int M_TO_MM = 1000;

    @Override
    protected void parseData(@NonNull BluetoothDevice device, @NonNull Data data, boolean isReceived) {
        if (data.size() != DATA_SIZE) {
            onInvalidDataReceived(device, data);
        } else {
            ByteBuffer buffer = ByteBuffer.wrap(data.getValue())
                    .order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer fb = buffer.asFloatBuffer();
            IntBuffer ib = buffer.asIntBuffer();

            boolean presence = ib.get() > 0 ? true : false;
            float distance = ((FloatBuffer) fb.position(ib.position())).get() * M_TO_MM;
            float presenceScore = fb.get();

            Log.d("RDC", "Parsed: " + presence + ", " + distance + ", " + presenceScore);
            PresenceResult result = new PresenceResult(presence, distance, presenceScore);
            onResultChanged(device, result, isReceived);
        }
    }
}
