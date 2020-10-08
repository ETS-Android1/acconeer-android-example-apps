package com.acconeer.bluetooth.distance.viewmodels;

import androidx.lifecycle.LiveData;

public class ScannerStateLiveData extends LiveData<ScannerStateLiveData> {
    private boolean hasScanningStarted;
    private boolean hasRecords;
    private boolean isBluetoothEnabled;
    private boolean isLocationEnabled;

    public ScannerStateLiveData(boolean isBluetoothEnabled, boolean isLocationEnabled) {
        this.isBluetoothEnabled = isBluetoothEnabled;
        this.isLocationEnabled = isLocationEnabled;
        this.hasRecords = false;
        this.hasScanningStarted = false;
    }

    public boolean isScanningStarted() {
        return hasScanningStarted;
    }

    public void scanningStarted() {
        this.hasScanningStarted = true;
        postValue(this);
    }

    public void refresh() {
        postValue(this);
    }

    public synchronized void scanningStopped() {
        this.hasScanningStarted = false;
        postValue(this);
    }

    public boolean hasRecords() {
        return hasRecords;
    }

    public void clearRecords() {
        hasRecords = false;
        postValue(this);
    }

    public void recordFound() {
        hasRecords = true;
        postValue(this);
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public void bluetoothEnabled() {
        isBluetoothEnabled = true;
        postValue(this);
    }

    public synchronized void bluetoothDisabled() {
        isBluetoothEnabled = false;
        hasRecords = false;
        postValue(this);
    }

    public boolean isLocationEnabled() {
        return isLocationEnabled;
    }

    public void locationEnabled() {
        isLocationEnabled = true;
        postValue(this);
    }

    public void locationDisabled() {
        isLocationEnabled = false;
        postValue(this);
    }
}
