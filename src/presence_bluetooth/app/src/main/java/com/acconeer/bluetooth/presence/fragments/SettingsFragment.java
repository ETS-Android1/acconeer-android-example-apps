package com.acconeer.bluetooth.presence.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.acconeer.bluetooth.presence.model.RadarCommand;
import com.acconeer.bluetooth.presence.util.Prefs;
import com.acconeer.bluetooth.presence.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.presence.views.preferences.ConfirmPreference;
import com.acconeer.bluetooth.presence.views.preferences.ScanPreference;
import com.acconeer.bluetooth.presence.views.preferences.StartPreference;
import com.acconeer.bluetooth.presence.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends PreferenceFragmentCompat {
    @BindView(R.id.toolbar2) Toolbar toolbar;

    private FragmentSwitcher switcher;

    private DeviceViewModel deviceViewModel;
    private boolean prefsChanged = false;

    public SettingsFragment() { }

    //Changes all of the settings to defaults for current profile if the profile changed
    private SharedPreferences.OnSharedPreferenceChangeListener onPrefChangeListener = (sharedPreferences, key) -> {
        if (key.equals(Prefs.PROFILE_KEY)) {
            EditTextPreference rangeStart = findPreference(Prefs.RANGE_START_KEY);
            EditTextPreference rangeLength = findPreference(Prefs.RANGE_LENGTH_KEY);
            EditTextPreference updateRate = findPreference(Prefs.UPDATE_RATE_KEY);
            EditTextPreference fixedThreshold = findPreference(Prefs.FIXED_THRESHOLD_KEY);

            rangeStart.setText(String.valueOf(Prefs.getDefault(Prefs.RANGE_START_KEY, getContext())));
            rangeLength.setText(String.valueOf(Prefs.getDefault(Prefs.RANGE_LENGTH_KEY, getContext())));
            updateRate.setText(String.valueOf(Prefs.getDefault(Prefs.UPDATE_RATE_KEY, getContext())));
            fixedThreshold.setText(String.valueOf(Prefs.getDefault(Prefs.FIXED_THRESHOLD_KEY, getContext())));
        }

        if (key.equals(Prefs.DISPLAY_DISTANCE_KEY)) {
            deviceViewModel.getDisplayDistance().postValue(Prefs.isDistanceDisplayed(getContext()));
        } else if (key.equals(Prefs.NUM_ZONES_KEY)) {
            deviceViewModel.getNumZones().postValue(Prefs.getNumZones(getContext()));
        } else {
            prefsChanged = true;
        }
    };

    private class UnitSummaryProvider implements Preference.SummaryProvider<EditTextPreference> {
        private String unit;

        public UnitSummaryProvider(String unit) {
            this.unit = unit;
        }

        @Override
        public CharSequence provideSummary(EditTextPreference preference) {
            if (preference.getText() == null) {
                return getString(R.string.not_set);
            } else {
                return preference.getText() + " " + unit;
            }
        }
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private void setValues() {
        this.<SeekBarPreference>findPreference(Prefs.PROFILE_KEY)
                .setValue(Prefs.getProfile(getContext()) - 1);
        this.<EditTextPreference>findPreference(Prefs.RANGE_START_KEY)
                .setText(String.valueOf(Prefs.getPref(Prefs.RANGE_START_KEY, getContext())));
        this.<EditTextPreference>findPreference(Prefs.RANGE_LENGTH_KEY)
                .setText(String.valueOf(Prefs.getPref(Prefs.RANGE_LENGTH_KEY, getContext())));
        this.<EditTextPreference>findPreference(Prefs.UPDATE_RATE_KEY)
                .setText(String.valueOf(Prefs.getPref(Prefs.UPDATE_RATE_KEY, getContext())));
        this.<EditTextPreference>findPreference(Prefs.FIXED_THRESHOLD_KEY)
                .setText(String.valueOf(Prefs.getPref(Prefs.FIXED_THRESHOLD_KEY, getContext())));
        this.<EditTextPreference>findPreference(Prefs.NUM_ZONES_KEY)
                .setText(String.valueOf(Prefs.getNumZones(getContext())));
        this.<SwitchPreference>findPreference(Prefs.DISPLAY_DISTANCE_KEY)
                .setChecked(Prefs.isDistanceDisplayed(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(onPrefChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(onPrefChangeListener);
    }

    private void setupStartButtons() {
        StartPreference start = findPreference(Prefs.START_MEASUREMENT_KEY);

        start.setOnClickListener(a -> {
            deviceViewModel.sendCommand(RadarCommand.SET);
            prefsChanged = false;
            switcher.setMain();
        });
    }

    private void setInputValidation() {
        configureEditText(Prefs.RANGE_START_KEY, e -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
        });
        configureEditText(Prefs.RANGE_LENGTH_KEY, e -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
        });
        configureEditText(Prefs.UPDATE_RATE_KEY, e -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        });
        configureEditText(Prefs.FIXED_THRESHOLD_KEY, e -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        });
        configureEditText(Prefs.NUM_ZONES_KEY, e -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
        });
    }

    private void configureEditText(String key, EditTextPreference.OnBindEditTextListener listener) {
        EditTextPreference edit = findPreference(key);

        edit.setOnBindEditTextListener(listener);
    }

    private void setSummaryProviders() {
        setUnitSummaryProvider(Prefs.RANGE_START_KEY, "mm");
        setUnitSummaryProvider(Prefs.RANGE_LENGTH_KEY, "mm");
        setUnitSummaryProvider(Prefs.UPDATE_RATE_KEY, "Hz");
        setUnitSummaryProvider(Prefs.FIXED_THRESHOLD_KEY, null);
        setUnitSummaryProvider(Prefs.NUM_ZONES_KEY, null);
    }

    private void setUnitSummaryProvider(String key, String unit) {
        if (unit != null) {
            this.<EditTextPreference>findPreference(key).setSummaryProvider(new UnitSummaryProvider(unit));
        } else {
            findPreference(key).setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;

        switch (preference.getKey()) {
            case Prefs.RESTORE_DEFAULTS_KEY:
                ConfirmPreference pref = (ConfirmPreference) preference;
                dialogFragment = pref.createPreferenceDialog(dialog -> {
                    deviceViewModel.sendCommand(RadarCommand.RESET);

                    //Initiate the change on the apps side as well
                    onPrefChangeListener.onSharedPreferenceChanged(null, Prefs.PROFILE_KEY);
                    prefsChanged = false;
                });
                break;
            case Prefs.CONNECTED_DEVICE_KEY:
                ScanPreference scanPreference = (ScanPreference) preference;
                dialogFragment = scanPreference.createPreferenceDialog();
                break;
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), "DialogTag");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentSwitcher) {
            switcher = (FragmentSwitcher) context;
        } else {
            throw new IllegalArgumentException("The containing Activity must implement FragmentSwitcher!");
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        setupStartButtons();
        setSummaryProviders();
        setInputValidation();
        setValues();

        deviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
        deviceViewModel.getDeviceName().observe(this, name -> {
            ScanPreference pref = findPreference(Prefs.CONNECTED_DEVICE_KEY);
            pref.setConnected(name);
        });
        deviceViewModel.getDeviceState().observe(this, new Observer<DeviceViewModel.DeviceState>() {
            private boolean ignoredFirst = false;

            @Override
            public void onChanged(DeviceViewModel.DeviceState state) {
                if (!ignoredFirst) {
                    ignoredFirst = true;

                    return;
                }
                switch (state) {
                    case NOT_CONNECTED:
                        break;
                    case CONNECTING:
                        Toast.makeText(SettingsFragment.this.getActivity(), R.string.connecting, Toast.LENGTH_SHORT).show();
                        break;
                    case NOT_SUPPORTED:
                        Toast.makeText(SettingsFragment.this.getActivity(), R.string.not_supported, Toast.LENGTH_LONG).show();
                        break;
                    case READY:
                        Toast.makeText(SettingsFragment.this.getActivity(), R.string.connected_success, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        deviceViewModel.getCommand().observe(this, c -> {
            Toast.makeText(getActivity(), "Command sent!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewParent = inflater.inflate(R.layout.fragment_settings, container, false);
        View viewPref = super.onCreateView(inflater, null, savedInstanceState);

        ViewGroup settingsParent = viewParent.findViewById(R.id.settings);
        settingsParent.addView(viewPref);

        ButterKnife.bind(this, viewParent);

        toolbar.setTitle(R.string.settings);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            if (prefsChanged) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.not_updated)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setMessage(R.string.message_leave_without_setting)
                        .setPositiveButton(R.string.leave, (dialog, which) -> switcher.setMain())
                        .setNeutralButton(R.string.send_and_leave, (dialog, which) -> {
                            deviceViewModel.sendCommand(RadarCommand.SET);
                            prefsChanged = false;
                            switcher.setMain();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                        .show();
            } else {
                switcher.setMain();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return viewParent;
    }
}
