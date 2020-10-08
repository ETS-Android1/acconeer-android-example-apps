package com.acconeer.bluetooth.distance.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import static android.bluetooth.BluetoothGattCharacteristic.*;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.acconeer.bluetooth.distance.bluetooth.callbacks.RadarManagerCallbacks;
import com.acconeer.bluetooth.distance.bluetooth.callbacks.data.CommandDataCallback;
import com.acconeer.bluetooth.distance.bluetooth.callbacks.data.ParametersDataCallback;
import com.acconeer.bluetooth.distance.bluetooth.callbacks.data.ResultDataCallback;
import com.acconeer.bluetooth.distance.bluetooth.callbacks.data.RssServiceDataCallback;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarCommand;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarParameters;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarResult;

import java.util.Arrays;
import java.util.UUID;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;

public class RadarManager extends BleManager<RadarManagerCallbacks> {
    private static final String TAG = "RadaManager";

    private static final UUID DISTANCE_SERVICE_UUID = UUID.fromString("ACC00001-052B-4D22-ABB3-787A852ABB7D");
    private static final UUID RSS_PROFILE_CHAR_UUID = UUID.fromString("ACC00002-052B-4D22-ABB3-787A852ABB7D");
    private static final UUID PARAMETERS_CHAR_UUID = UUID.fromString("ACC00003-052B-4D22-ABB3-787A852ABB7D");
    private static final UUID RESULT_CHAR_UUID = UUID.fromString("ACC00004-052B-4D22-ABB3-787A852ABB7D");
    private static final UUID COMMAND_CHAR_UUID = UUID.fromString("ACC00005-052B-4D22-ABB3-787A852ABB7D");

    private BluetoothGattCharacteristic rssProfileChar;
    private BluetoothGattCharacteristic parametersChar;
    private BluetoothGattCharacteristic resultChar;
    private BluetoothGattCharacteristic commandChar;

    private BleManagerGattCallback bleCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
//            enableNotifications(parametersChar).enqueue();
            enableNotifications(resultChar).enqueue();

//            setNotificationCallback(parametersChar).with(parametersCallback);
            setNotificationCallback(resultChar).with(resultCallback);
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(DISTANCE_SERVICE_UUID);
            if (service == null) return false;

            rssProfileChar = service.getCharacteristic(RSS_PROFILE_CHAR_UUID);
            parametersChar = service.getCharacteristic(PARAMETERS_CHAR_UUID);
            resultChar = service.getCharacteristic(RESULT_CHAR_UUID);
            commandChar = service.getCharacteristic(COMMAND_CHAR_UUID);
            if (rssProfileChar == null || parametersChar == null || resultChar == null
                || commandChar == null) {
                return false;
            }

            if (lacksRequiredProperties(rssProfileChar, PROPERTY_READ, PROPERTY_WRITE_NO_RESPONSE)) return false;
            if (lacksRequiredProperties(parametersChar, PROPERTY_READ, PROPERTY_WRITE_NO_RESPONSE,
                    PROPERTY_NOTIFY)) return false;
            if (lacksRequiredProperties(resultChar, PROPERTY_READ, PROPERTY_NOTIFY)) return false;
            if (lacksRequiredProperties(commandChar, PROPERTY_WRITE_NO_RESPONSE)) return false;

            return true;
        }

        private boolean lacksRequiredProperties(BluetoothGattCharacteristic characteristic, int... properties) {
            int props = characteristic.getProperties();

            for (int propertyMask : properties) {
                if ((props & propertyMask) <= 0) return true;
            }

            return false;
        }

        @Override
        protected void onDeviceDisconnected() {
            rssProfileChar = null;
            parametersChar = null;
            resultChar = null;
            commandChar = null;
        }
    };

    private RssServiceDataCallback rssCallback = new RssServiceDataCallback() {
        @Override
        public void onRssProfileChanged(@NonNull BluetoothDevice device, int value, boolean isReceived) {
            mCallbacks.onRssProfileChanged(device, value, isReceived);
            Log.d(TAG, "RSS Profile callback: " + value);
        }
    };

    private ParametersDataCallback parametersCallback = new ParametersDataCallback() {
        @Override
        public void onParametersChanged(@NonNull BluetoothDevice device, RadarParameters parameters,
                boolean isReceived) {
            Log.d(TAG, "Parameters callback: " + parameters);
            mCallbacks.onParametersChanged(device, parameters, isReceived);
        }
    };

    private ResultDataCallback resultCallback = new ResultDataCallback() {
        @Override
        public void onResultChanged(@NonNull BluetoothDevice device, RadarResult result, boolean isReceived) {
            Log.d(TAG, "Result callback: " + result);
            mCallbacks.onResultChanged(device, result, isReceived);
        }
    };

    private CommandDataCallback commandCallback = new CommandDataCallback() {
        @Override
        public void onCommand(@NonNull BluetoothDevice device, RadarCommand command, boolean isReceived) {
            Log.d(TAG, "Command callback: " + command);
            mCallbacks.onCommand(device, command, isReceived);
        }
    };

    public RadarManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return bleCallback;
    }

    public void sendRssProfile(byte value) {
        if (rssProfileChar != null) {
            writeCharacteristic(rssProfileChar, Data.opCode(value))
                    .with(rssCallback).enqueue();
            Log.d(TAG, "Writing profile: " + Data.opCode(value));
        }
    }

    public void sendParameters(RadarParameters parameters) {
        if (parametersChar != null) {
            writeCharacteristic(parametersChar, parameters.toByteArray())
                    .with(parametersCallback).enqueue();
            Log.d(TAG, "Writing parameters: " + Arrays.toString(parameters.toByteArray()));
        }
    }

    public void readResult() {
        if (resultChar != null) {
            readCharacteristic(resultChar)
                    .with(resultCallback).enqueue();
        }
    }

    public void writeCommand(RadarCommand command) {
        if (commandCallback != null) {
            Integer commandOrdinal = command.ordinal();
            writeCharacteristic(commandChar, Data.opCode(commandOrdinal.byteValue()))
                    .with(commandCallback).enqueue();
            Log.d(TAG, "Writing command: " + Data.opCode(commandOrdinal.byteValue()));
        }
    }
}
