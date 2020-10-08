package com.acconeer.bluetooth.distance.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreference.OnBindEditTextListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarCommand;
import com.acconeer.bluetooth.distance.utils.Prefs;
import com.acconeer.bluetooth.distance.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.distance.views.preferences.ConfirmPreference;
import com.acconeer.bluetooth.distance.views.preferences.ScanPreference;
import com.acconeer.bluetooth.distance.views.preferences.StartPreference;

import static com.acconeer.bluetooth.distance.utils.Prefs.BACKGROUND_CLUTTER_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.CONNECTED_DEVICE_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.FIXED_THRESHOLD_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.GAIN_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.PROFILE_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.RANGE_LENGTH_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.RANGE_START_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.RESTORE_DEFAULTS_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.START_MEASUREMENT_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.UPDATE_RATE_KEY;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        toolbar = findViewById(R.id.toolbar2);

        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            if (settingsFragment.isPrefsChanged()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.not_updated)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setMessage(R.string.message_leave_without_setting)
                        .setPositiveButton(R.string.leave, (dialog, which) -> NavUtils.navigateUpFromSameTask(this))
                        .setNeutralButton(R.string.send_and_leave, (dialog, which) -> {
                            DeviceViewModel.getInstance(getApplication()).sendCommand(RadarCommand.SET);
                            NavUtils.navigateUpFromSameTask(this);
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                        .show();
            } else {
                NavUtils.navigateUpFromSameTask(this);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private DeviceViewModel deviceViewModel;
        private boolean prefsChanged = false;

        //Changes all of the settings to defaults for current profile if the profile changed
        private SharedPreferences.OnSharedPreferenceChangeListener onPrefChangeListener = (sharedPreferences, key) -> {
            if (key.equals(Prefs.PROFILE_KEY)) {
                EditTextPreference rangeStart = findPreference(RANGE_START_KEY);
                EditTextPreference rangeLength = findPreference(RANGE_LENGTH_KEY);
                EditTextPreference gain = findPreference(GAIN_KEY);
                EditTextPreference updateRate = findPreference(UPDATE_RATE_KEY);
                EditTextPreference fixedThreshold = findPreference(FIXED_THRESHOLD_KEY);

                rangeStart.setText(String.valueOf(Prefs.getDefault(RANGE_START_KEY, getContext())));
                rangeLength.setText(String.valueOf(Prefs.getDefault(RANGE_LENGTH_KEY, getContext())));
                gain.setText(String.valueOf(Prefs.getDefault(GAIN_KEY, getContext())));
                updateRate.setText(String.valueOf(Prefs.getDefault(UPDATE_RATE_KEY, getContext())));
                fixedThreshold.setText(String.valueOf(Prefs.getDefault(FIXED_THRESHOLD_KEY, getContext())));
            }

            prefsChanged = true;
        };

        public boolean isPrefsChanged() {
            return prefsChanged;
        }

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

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            setupStartButtons();
            setSummaryProviders();
            setInputValidation();
            setValues();

            deviceViewModel = DeviceViewModel.getInstance(getActivity().getApplication());
            deviceViewModel.getDeviceName().observe(this, name -> {
                ScanPreference pref = findPreference(CONNECTED_DEVICE_KEY);
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

        private void setValues() {
            this.<EditTextPreference>findPreference(RANGE_START_KEY)
                    .setText(String.valueOf(Prefs.getPref(RANGE_START_KEY, getContext())));
            this.<EditTextPreference>findPreference(RANGE_LENGTH_KEY)
                    .setText(String.valueOf(Prefs.getPref(RANGE_LENGTH_KEY, getContext())));
            this.<EditTextPreference>findPreference(GAIN_KEY)
                    .setText(String.valueOf(Prefs.getPref(GAIN_KEY, getContext())));
            this.<EditTextPreference>findPreference(UPDATE_RATE_KEY)
                    .setText(String.valueOf(Prefs.getPref(UPDATE_RATE_KEY, getContext())));
            this.<EditTextPreference>findPreference(FIXED_THRESHOLD_KEY)
                    .setText(String.valueOf(Prefs.getPref(FIXED_THRESHOLD_KEY, getContext())));
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
            StartPreference start = findPreference(START_MEASUREMENT_KEY);
            StartPreference noiseStart = findPreference(BACKGROUND_CLUTTER_KEY);

            start.setOnClickListener(a -> {
                deviceViewModel.sendCommand(RadarCommand.SET);
                prefsChanged = false;
                NavUtils.navigateUpFromSameTask(getActivity());
            });
            noiseStart.setOnClickListener(a -> {
                deviceViewModel.sendCommand(RadarCommand.THRESHOLD_ESTIMATION);
                prefsChanged = false;
                NavUtils.navigateUpFromSameTask(getActivity());
            });

            //TODO: Input validation
        }

        private void setInputValidation() {
            configureEditText(RANGE_START_KEY, e -> {
                e.setInputType(InputType.TYPE_CLASS_NUMBER);
            });
            configureEditText(RANGE_LENGTH_KEY, e -> {
                e.setInputType(InputType.TYPE_CLASS_NUMBER);
            });
            configureEditText(GAIN_KEY, e -> {
                e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            });
            configureEditText(UPDATE_RATE_KEY, e -> {
                e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            });
            configureEditText(FIXED_THRESHOLD_KEY, e -> {
                e.setInputType(InputType.TYPE_CLASS_NUMBER);
            });
        }

        private void configureEditText(String key, OnBindEditTextListener listener) {
            EditTextPreference edit = findPreference(key);

            edit.setOnBindEditTextListener(listener);
        }

        private void setSummaryProviders() {
            setUnitSummaryProvider(RANGE_START_KEY, "mm");
            setUnitSummaryProvider(RANGE_LENGTH_KEY, "mm");
            setUnitSummaryProvider(GAIN_KEY, null);
            setUnitSummaryProvider(UPDATE_RATE_KEY, "Hz");
            setUnitSummaryProvider(FIXED_THRESHOLD_KEY, null);
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
                case RESTORE_DEFAULTS_KEY:
                    ConfirmPreference pref = (ConfirmPreference) preference;
                    dialogFragment = pref.createPreferenceDialog(dialog -> {
                        deviceViewModel.sendCommand(RadarCommand.RESET);

                        //Initiate the change on the apps side as well
                        onPrefChangeListener.onSharedPreferenceChanged(null, PROFILE_KEY);
                        prefsChanged = false;
                    });
                    break;
                case CONNECTED_DEVICE_KEY:
                    ScanPreference scanPreference = (ScanPreference) preference;
                    dialogFragment = scanPreference.createPreferenceDialog();
                    break;
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(), "DialogTag");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}