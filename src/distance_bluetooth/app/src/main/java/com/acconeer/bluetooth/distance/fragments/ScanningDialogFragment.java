package com.acconeer.bluetooth.distance.fragments;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acconeer.bluetooth.distance.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.distance.viewmodels.ScannerStateLiveData;
import com.acconeer.bluetooth.distance.viewmodels.ScannerViewModel;
import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.adapters.FoundDeviceAdapter;
import com.acconeer.bluetooth.distance.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScanningDialogFragment extends PreferenceDialogFragmentCompat implements FoundDeviceAdapter.OnItemClickListener {
    private static final int LOCATION_PERM_REQUEST_CODE = 4444;

    private RecyclerView devicesRecycler;
    private FoundDeviceAdapter deviceAdapter;
    private LinearLayout noBleLayout, noLocationPermLayout, noLocationLayout;
    private Button enableBleButton, grantLocationButton, enableLocationButton;
    private ProgressBar progressBar;
    private TextView noDevicesText;

    private boolean scanningKilled = false;

    private ScannerViewModel scannerViewModel;
    private DeviceViewModel deviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel.class);
        scannerViewModel.getScannerState().observe(this, this::startScanning);
        scannerViewModel.getScannerState().observe(this, this::updateViews);

        deviceViewModel = DeviceViewModel.getInstance(getActivity().getApplication());
        deviceViewModel.getConnectionState().observe(this, connected -> {
            if (connected) {
                getDialog().dismiss();
            }
        });
    }

    private void updateViews(ScannerStateLiveData scannerStateLiveData) {
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (scannerStateLiveData.isScanningStarted()) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.stop_scan);

            if (scannerStateLiveData.hasRecords()) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.start_scan);

            progressBar.setVisibility(View.GONE);
        }

    }

    private void startScanning(ScannerStateLiveData scannerStateLiveData) {
        //Dont restart scanning if it has been explicitly stopped
        if (scanningKilled) {
            scanningKilled = false;
            return;
        }

        if (Utils.hasLocationPermission(getContext())) {
            noLocationPermLayout.setVisibility(View.GONE);

            if (Utils.isLocationEnabled(getContext())) {
                noLocationLayout.setVisibility(View.GONE);

                if (Utils.isBleEnabled()) {
                    noBleLayout.setVisibility(View.GONE);
                    scannerViewModel.startScan();

                    if (!scannerStateLiveData.isScanningStarted() || scannerStateLiveData.hasRecords()) {
                        noDevicesText.setVisibility(View.GONE);
                    } else {
                        noDevicesText.setVisibility(View.VISIBLE);
                    }
                } else {
                    noBleLayout.setVisibility(View.VISIBLE);
                    noDevicesText.setVisibility(View.GONE);
                }
            } else {
                noLocationLayout.setVisibility(View.VISIBLE);
                noDevicesText.setVisibility(View.GONE);
                noBleLayout.setVisibility(View.GONE);
            }
        } else {
            noLocationPermLayout.setVisibility(View.VISIBLE);
            noDevicesText.setVisibility(View.GONE);
            noBleLayout.setVisibility(View.GONE);
            noLocationLayout.setVisibility(View.GONE);
        }
    }

    public static ScanningDialogFragment newInstance(String key) {
        ScanningDialogFragment fragment = new ScanningDialogFragment();

        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);

        return fragment;
    }

    private void getReferences(View root) {
        devicesRecycler = root.findViewById(R.id.devices_recycler);
        noDevicesText = root.findViewById(R.id.no_devices);
        noBleLayout = root.findViewById(R.id.ble_disabled);
        noLocationPermLayout = root.findViewById(R.id.location_perm);
        noLocationLayout = root.findViewById(R.id.location_disabled);
        enableBleButton = root.findViewById(R.id.enable_ble);
        grantLocationButton = root.findViewById(R.id.grant_location);
        enableLocationButton = root.findViewById(R.id.enable_location);
        progressBar = root.findViewById(R.id.searching_progress);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        getReferences(view);
        setUpRecycler();
        setUpButtons();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog d = (AlertDialog) super.onCreateDialog(savedInstanceState);

        //getButton is null before dialog is shown
        d.setOnShowListener(dialog -> {
            //Override the listeners here to prevent dialog clone on button click
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!scannerViewModel.getScannerState().isScanningStarted()) {
                    scannerViewModel.getScannerState().clearRecords();
                    startScanning(scannerViewModel.getScannerState());
                } else {
                    scannerViewModel.stopScan();
                    scanningKilled = true;
                }
            });
        });

        return d;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            scannerViewModel.stopScan();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startScanning(scannerViewModel.getScannerState());
    }

    private void setUpButtons() {
        enableLocationButton.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });
        enableBleButton.setOnClickListener(v -> {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        });
        grantLocationButton.setOnClickListener(v -> {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERM_REQUEST_CODE);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        scannerViewModel.notifyPermChange();
    }

    private void setUpRecycler() {
        devicesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new FoundDeviceAdapter(this, scannerViewModel.getRadars(), this);
        devicesRecycler.setAdapter(deviceAdapter);
    }

    @Override
    public void onItemClicked(BluetoothDevice device) {
        deviceViewModel.setDevice(device);
    }
}
