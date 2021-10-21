package com.acconeer.bluetooth.presence.fragments;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.acconeer.bluetooth.presence.adapters.FoundDeviceAdapter;
import com.acconeer.bluetooth.presence.livedata.ScannerStateLiveData;
import com.acconeer.bluetooth.presence.util.Utils;
import com.acconeer.bluetooth.presence.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.presence.viewmodels.ScannerViewModel;
import com.acconeer.bluetooth.presence.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanningDialogFragment extends PreferenceDialogFragmentCompat implements FoundDeviceAdapter.OnItemClickListener {
    private static final int LOCATION_PERM_REQUEST_CODE = 4444;

    @BindView(R.id.devices_recycler) RecyclerView devicesRecycler;
    @BindView(R.id.ble_disabled) LinearLayout noBleLayout;
    @BindView(R.id.location_perm) LinearLayout noLocationPermLayout;
    @BindView(R.id.location_disabled) LinearLayout noLocationLayout;
    @BindView(R.id.enable_ble) Button enableBleButton;
    @BindView(R.id.grant_location) Button grantLocationButton;
    @BindView(R.id.enable_location) Button enableLocationButton;
    @BindView(R.id.searching_progress) ProgressBar progressBar;
    @BindView(R.id.no_devices) TextView noDevicesText;

    private FoundDeviceAdapter deviceAdapter;

    private boolean scanningKilled = false;

    private ScannerViewModel scannerViewModel;
    private DeviceViewModel deviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel.class);
        scannerViewModel.getScannerState().observe(this, this::startScanning);
        scannerViewModel.getScannerState().observe(this, this::updateViews);

        deviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
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

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        ButterKnife.bind(this, view);

        setUpRecycler();
        setUpButtons();

        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
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
