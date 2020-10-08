package com.acconeer.bluetooth.distance.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.acconeer.bluetooth.distance.bluetooth.RadarManager;
import com.acconeer.bluetooth.distance.bluetooth.callbacks.RadarManagerCallbacks;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarCommand;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarParameters;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarResult;
import com.acconeer.bluetooth.distance.utils.Prefs;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DeviceViewModel extends AndroidViewModel implements RadarManagerCallbacks {
    private static DeviceViewModel instance;

    private RadarManager manager;

    private MutableLiveData<BluetoothDevice> deviceLiveData;
    private MutableLiveData<String> connectedDeviceName;
    private MutableLiveData<Boolean> connectionState;
    private MutableLiveData<DeviceState> deviceState;

    private MutableLiveData<Integer> rssProfile;
    private MutableLiveData<RadarParameters> parameters;
    private MutableLiveData<RadarResult> result;
    private SingleLiveEvent<RadarCommand> command;

    public enum DeviceState {
        NOT_CONNECTED, CONNECTING, NOT_SUPPORTED, READY;

    }

    protected DeviceViewModel(Application application) {
        super(application);

        connectionState = new MutableLiveData<>();
        deviceState = new MutableLiveData<>(DeviceState.NOT_CONNECTED);
        deviceLiveData = new MutableLiveData<>();
        connectedDeviceName = new MutableLiveData<>();

        rssProfile = new MutableLiveData<>();
        parameters = new MutableLiveData<>();
        result = new MutableLiveData<>();
        command = new SingleLiveEvent<>();

        this.manager = new RadarManager(application);
        manager.setGattCallbacks(this);
    }

    public static DeviceViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new DeviceViewModel(application);
        }

        return instance;
    }

    @MainThread
    public void setDevice(BluetoothDevice device) {
        if (deviceLiveData.getValue() == null) {
            //Need to use setValue because the connect method depends on the values
            deviceLiveData.setValue(device);
            deviceState.setValue(DeviceState.CONNECTING);

            connect();
        }
    }

    public LiveData<String> getDeviceName() {
        return connectedDeviceName;
    }

    public LiveData<Boolean> getConnectionState() {
        return connectionState;
    }

    public LiveData<DeviceState> getDeviceState() {
        return deviceState;
    }

    public LiveData<Integer> getRssProfile() {
        return rssProfile;
    }

    public LiveData<RadarParameters> getParameters() {
        return parameters;
    }

    public LiveData<RadarResult> getResult() {
        return result;
    }

    public LiveData<RadarCommand> getCommand() {
        return command;
    }

    private void connect() {
        if (deviceLiveData.getValue() != null) {
            manager.connect(deviceLiveData.getValue())
                    .retry(3, 400)
                    .useAutoConnect(false)
                    .enqueue();
            deviceState.setValue(DeviceState.CONNECTING);
        }
    }

    public void disconnect() {
        if (manager.isConnected()) {
            manager.disconnect().enqueue();
            deviceLiveData.postValue(null);
            connectedDeviceName.postValue(null);
            deviceState.postValue(DeviceState.NOT_CONNECTED);
        }
    }

    protected void setRssProfile() {
        Integer profile = Prefs.getProfile(getApplication());
        manager.sendRssProfile(profile.byteValue());
        rssProfile.postValue(profile);
    }

    protected void setParameters() {
        RadarParameters params = Prefs.getParams(getApplication());
        manager.sendParameters(params);
        parameters.postValue(params);
    }

    public void sendCommand(RadarCommand cmd) {
        if (cmd != RadarCommand.RESET) {
            // Order important. Profile first
            setRssProfile();
            setParameters();
        }

        manager.writeCommand(cmd);
        command.postValue(cmd);
    }

    public void readResult() {
        manager.readResult();
    }

    @Override
    protected void onCleared() {
        disconnect();
    }

    @Override
    public void onCommand(@NonNull BluetoothDevice device, RadarCommand command, boolean isReceived) {

    }

    @Override
    public void onResultChanged(@NonNull BluetoothDevice device, RadarResult res, boolean isReceived) {
        result.postValue(res);
    }

    @Override
    public void onParametersChanged(@NonNull BluetoothDevice device, RadarParameters params, boolean isReceived) {
        parameters.postValue(params);
    }

    @Override
    public void onRssProfileChanged(@NonNull BluetoothDevice device, int value, boolean isReceived) {
        rssProfile.postValue(value);
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        connectionState.postValue(true);
        connectedDeviceName.postValue(device.getName());
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        connectionState.postValue(false);
        deviceLiveData.postValue(null);
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        connectionState.postValue(false);
        deviceLiveData.postValue(null);
        connectedDeviceName.postValue(null);
        deviceState.postValue(DeviceState.NOT_CONNECTED);
        result.postValue(null);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        deviceState.postValue(DeviceState.READY);
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        Log.d("TAG", "onBondingRequired");
    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        Log.d("TAG", "onBonded");
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        Log.d("TAG", "onBondingFailed");
    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
        deviceState.postValue(DeviceState.NOT_SUPPORTED);

        manager.disconnect();
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
        //This is only called if autoConnect is used- we dont use it.
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
        //initialize already takes care of the setup for us
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        //Useless
    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
        Log.d("DVM", "Error: " + errorCode + ": " + message);
    }
}
