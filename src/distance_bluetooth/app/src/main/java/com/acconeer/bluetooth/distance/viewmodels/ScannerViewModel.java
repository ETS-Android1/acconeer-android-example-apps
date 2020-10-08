package com.acconeer.bluetooth.distance.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import com.acconeer.bluetooth.distance.utils.Utils;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScannerViewModel extends AndroidViewModel {
    private static final String MODULE_NAME = "XM122 IoT Module";
    private static final int ACCONEER_ID = 0x2A29;

    private ScannerStateLiveData scannerStateLiveData;
    private DeviceSetLiveData radarsLiveData;

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            if (radarsLiveData.addDevice(result)) {
                scannerStateLiveData.recordFound();
            }
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            scannerStateLiveData.scanningStopped();
        }
    };

    private BroadcastReceiver bleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.STATE_OFF);
            int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                    BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    scannerStateLiveData.bluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF &&
                            previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan();
                        scannerStateLiveData.bluetoothDisabled();
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver locationStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isLocationEnabled(context)) {
                scannerStateLiveData.locationEnabled();
            } else {
                scannerStateLiveData.locationDisabled();
            }
        }
    };

    public ScannerViewModel(@NonNull Application application) {
        super(application);

        scannerStateLiveData = new ScannerStateLiveData(Utils.isBleEnabled(),
                Utils.isLocationEnabled(application));
        radarsLiveData = new DeviceSetLiveData();

        registerChangeReceivers(application);
    }

    public DeviceSetLiveData getRadars() {
        return radarsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        getApplication().unregisterReceiver(locationStateReceiver);
        getApplication().unregisterReceiver(bleStateReceiver);
    }

    private void registerChangeReceivers(Application application) {
        application.registerReceiver(bleStateReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        application.registerReceiver(locationStateReceiver,
                new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
    }

    public ScannerStateLiveData getScannerState() {
        return scannerStateLiveData;
    }

    public void startScan() {
        if (!scannerStateLiveData.isScanningStarted()) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            List<ScanFilter> filters = Collections.singletonList(
                    new ScanFilter.Builder().setDeviceName(MODULE_NAME).build());
            BluetoothLeScannerCompat.getScanner().startScan(filters, settings, scanCallback);
            scannerStateLiveData.scanningStarted();
            Log.d("ScannerVM", "Scanning started");
        }
    }

    public void stopScan() {
        if (scannerStateLiveData.isScanningStarted() && scannerStateLiveData.isBluetoothEnabled()) {
            BluetoothLeScannerCompat.getScanner().stopScan(scanCallback);
            scannerStateLiveData.scanningStopped();
            Log.d("ScannerVM", "Scanning stopped");
        }
    }

    public void notifyPermChange() {
        scannerStateLiveData.refresh();
    }
}
