package com.acconeer.bluetooth.presence.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.acconeer.bluetooth.presence.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.presence.R;
import com.acconeer.bluetooth.presence.fragments.FragmentSwitcher;
import com.acconeer.bluetooth.presence.fragments.MainFragment;
import com.acconeer.bluetooth.presence.fragments.SettingsFragment;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements FragmentSwitcher {
    private static final long DISCONNECT_TIME = 2 * 60 * 1000;

    private MainFragment mainFragment;
    private SettingsFragment settingsFragment;

    private static Timer timer = new Timer();
    private DisconnectTimerTask disconnectTask;

    private class DisconnectTimerTask extends TimerTask {
        @Override
        public void run() {
            ViewModelProviders.of(MainActivity.this).get(DeviceViewModel.class).disconnect();
            Log.d("LifeObserver", "OnTimer");
        }
    };

    private LifecycleEventObserver onLifecycleChange = (source, event) -> {
        if (event == Lifecycle.Event.ON_DESTROY) {
            ViewModelProviders.of(MainActivity.this).get(DeviceViewModel.class).disconnect();
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = MainFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setMain();

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(onLifecycleChange);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(onLifecycleChange);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProcessLifecycleOwner.get().getLifecycle().removeObserver(onLifecycleChange);
    }

    @Override
    public void setMain() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mainFragment)
                .commit();
    }

    @Override
    public void setSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, settingsFragment)
                .commit();
    }
}
