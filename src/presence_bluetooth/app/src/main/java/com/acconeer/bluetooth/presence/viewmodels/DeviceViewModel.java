package com.acconeer.bluetooth.presence.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.acconeer.bluetooth.presence.bluetooth.RadarManager;
import com.acconeer.bluetooth.presence.bluetooth.callbacks.RadarManagerCallbacks;
import com.acconeer.bluetooth.presence.livedata.SingleLiveEvent;
import com.acconeer.bluetooth.presence.model.PresenceResult;
import com.acconeer.bluetooth.presence.model.RadarCommand;
import com.acconeer.bluetooth.presence.model.RadarParameters;
import com.acconeer.bluetooth.presence.util.Prefs;

public class DeviceViewModel extends AndroidViewModel implements RadarManagerCallbacks {
    private RadarManager manager;

    private MutableLiveData<BluetoothDevice> deviceLiveData;
    private MutableLiveData<String> connectedDeviceName;
    private MutableLiveData<Boolean> connectionState;
    private MutableLiveData<DeviceState> deviceState;

    private MutableLiveData<Integer> rssProfile;
    private MutableLiveData<RadarParameters> parameters;
    private MutableLiveData<PresenceResult> result;
    private SingleLiveEvent<RadarCommand> command;

    private MutableLiveData<Integer> numZones;
    private MutableLiveData<Boolean> displayDistance;

    public enum DeviceState {
        NOT_CONNECTED, CONNECTING, NOT_SUPPORTED, READY;

    }

    public DeviceViewModel(Application application) {
        super(application);

        connectionState = new MutableLiveData<>();
        deviceState = new MutableLiveData<>(DeviceState.NOT_CONNECTED);
        deviceLiveData = new MutableLiveData<>();
        connectedDeviceName = new MutableLiveData<>();

        rssProfile = new MutableLiveData<>();
        parameters = new MutableLiveData<>();
        result = new MutableLiveData<>();
        command = new SingleLiveEvent<>();

        numZones = new MutableLiveData<>();
        displayDistance = new MutableLiveData<>();

        this.manager = new RadarManager(application);
        manager.setGattCallbacks(this);

        initFromSettings();
    }

    public void initFromSettings() {
        Integer profile = Prefs.getProfile(getApplication());
        RadarParameters params = Prefs.getParams(getApplication());
        int numberZones = Prefs.getNumZones(getApplication());
        boolean isDistanceDisplayed = Prefs.isDistanceDisplayed(getApplication());

        displayDistance.postValue(isDistanceDisplayed);
        numZones.postValue(numberZones);
        result.postValue(null);
        connectedDeviceName.postValue(null);
        deviceLiveData.postValue(null);
        deviceState.postValue(DeviceState.NOT_CONNECTED);
        connectionState.postValue(false);
        rssProfile.postValue(profile);
        parameters.postValue(params);
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

    public LiveData<PresenceResult> getResult() {
        return result;
    }

    public LiveData<RadarCommand> getCommand() {
        return command;
    }

    public MutableLiveData<Integer> getNumZones() {
        return numZones;
    }

    public MutableLiveData<Boolean> getDisplayDistance() {
        return displayDistance;
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
    public void onResultChanged(@NonNull BluetoothDevice device, PresenceResult res, boolean isReceived) {
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
