package com.acconeer.bluetooth.distance.viewmodels;

import android.bluetooth.BluetoothDevice;

import java.util.HashSet;
import java.util.Set;

import androidx.lifecycle.LiveData;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DeviceSetLiveData extends LiveData<Set<BluetoothDevice>> {
    public DeviceSetLiveData() {
        super(new HashSet<>());
    }

    public boolean addDevice(ScanResult scanResult) {
        if (isRadar(scanResult)) {
            boolean res = getValue().add(scanResult.getDevice());

            if (res) {
                postValue(getValue());
            }

            return res;
        } else {
            return false;
        }
    }

    public void clear() {
        getValue().clear();

        postValue(getValue());
    }

    private boolean isRadar(ScanResult result) {
        return true; //There is nothing in the ad packet to tell
//        ScanRecord record = result.getScanRecord();
//        if (record == null) return false;
//        List<ParcelUuid> uuids = record.getServiceUuids();
//        if (uuids == null) return false;
//        return uuids.contains(RADAR_SERVICE_UUID);
    }
}
