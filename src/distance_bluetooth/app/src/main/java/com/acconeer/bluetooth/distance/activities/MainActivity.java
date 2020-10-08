package com.acconeer.bluetooth.distance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.acconeer.bluetooth.distance.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarParameters;
import com.acconeer.bluetooth.distance.bluetooth.data.RadarResult;
import com.acconeer.bluetooth.distance.utils.Prefs;
import com.acconeer.bluetooth.distance.views.ConnectionButton;
import com.acconeer.bluetooth.distance.views.DistanceGauge;
import com.acconeer.bluetooth.distance.views.LabeledImageButton;
import com.acconeer.bluetooth.distance.views.SignalStrengthIndicator;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static com.acconeer.bluetooth.distance.utils.Prefs.RANGE_LENGTH_KEY;
import static com.acconeer.bluetooth.distance.utils.Prefs.RANGE_START_KEY;

public class MainActivity extends AppCompatActivity {
    private static final int[] SIGNAL_BOUNDARIES = new int[] {250, 500, 1000, 1500, 3000, 4000};
    private static final long DISCONNECT_TIME = 2 * 60 * 1000;

    private Toolbar toolbar;
    private DistanceGauge distanceGauge;
    private SignalStrengthIndicator signalStrengthIndicator;
    private LabeledImageButton warningButton;
    private ConnectionButton connectionButton;
    private TextView distanceText;

    private DeviceViewModel deviceViewModel;

    private DecimalFormat format = new DecimalFormat("#");

    private static Boolean previousConnectionState = null; //TODO: Dirty hack
    private static Timer timer = new Timer();
    private DisconnectTimerTask disconnectTask;

    private class DisconnectTimerTask extends TimerTask {
        @Override
        public void run() {
            deviceViewModel.disconnect();
            Log.d("LifeObserver", "OnTimer");
        }
    };

    private LifecycleEventObserver onLifecycleChange = (source, event) -> {
        if (event == Lifecycle.Event.ON_DESTROY) {
            deviceViewModel.disconnect();
            Log.d("LifeObserver", "App closed, disconnecting from device ");
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            if (disconnectTask != null) {
                disconnectTask.cancel();
            }
            disconnectTask = new DisconnectTimerTask();
            timer.schedule(disconnectTask, DISCONNECT_TIME);
            Log.d("LifeObserver", "App paused, started disconnect timer ");
        } else if (event == Lifecycle.Event.ON_RESUME) {
            if (disconnectTask != null) {
                disconnectTask.cancel();
            }
            Log.d("LifeObserver", "App resumed, canceled disconnect timer ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getReferences();

        toolbar.setTitle(R.string.distance_measurement);
        setSupportActionBar(toolbar);

        connectionButton.setConnectAction(v -> startSettings());
        connectionButton.setDisconnected();
        warningButton.setOnClickListener(v -> startSettings());

        float start = Prefs.getPref(RANGE_START_KEY, this);
        float length = Prefs.getPref(RANGE_LENGTH_KEY, this);
        distanceGauge.setStart((int) start);
        distanceGauge.setEnd((int) (start + length));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        deviceViewModel = DeviceViewModel.getInstance(getApplication());
        deviceViewModel.getDeviceName().observe(this, this::onDeviceChange);
        deviceViewModel.getParameters().observe(this, this::onParamsChange);
        deviceViewModel.getResult().observe(this, this::onResultChange);
        deviceViewModel.getConnectionState().observe(this, current -> {
            // If we went from connected to disconnected
            Log.d("CS", "Previous: " + previousConnectionState + ", current: " + current);
            if (previousConnectionState != null && previousConnectionState && !current) {
                Toast.makeText(MainActivity.this,
                        MainActivity.this.getString(R.string.disconnected),
                        Toast.LENGTH_SHORT).show();
            }

            previousConnectionState = current;
        });

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(onLifecycleChange);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(onLifecycleChange);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(onLifecycleChange);
    }

    private void onResultChange(RadarResult radarResult) {
        if (radarResult == null) {
            distanceText.setText(MainActivity.this.getString(R.string.no_distance_indicator));
            warningButton.showAnimated(false);
            distanceGauge.setLevel(0);
            signalStrengthIndicator.setLevel(0);
        } else {
            distanceGauge.setLevel((int) radarResult.distance);
            signalStrengthIndicator.setSignalStrength(radarResult.signalStrength, SIGNAL_BOUNDARIES);
            distanceText.setText(format.format(radarResult.distance));
            warningButton.showAnimated(radarResult.isSaturated);
        }
    }

    private void onParamsChange(RadarParameters parameters) {
        distanceGauge.setStart((int) parameters.rangeStart);
        distanceGauge.setEnd((int) (parameters.rangeStart + parameters.rangeLength));
    }

    private void onDeviceChange(String name) {
        if (name == null) {
            connectionButton.setDisconnected();
        } else {
            connectionButton.setConnected(name);
        }
    }

    private void getReferences() {
        toolbar = findViewById(R.id.toolbar);
        distanceGauge = findViewById(R.id.gauge);
        signalStrengthIndicator = findViewById(R.id.signal_strength);
        warningButton = findViewById(R.id.warningButton);
        connectionButton = findViewById(R.id.connectionButton);
        distanceText = findViewById(R.id.distance_text);

        //TODO: Testing
        distanceGauge.setOnClickListener(v -> {
            deviceViewModel.readResult();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startSettings();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
}
