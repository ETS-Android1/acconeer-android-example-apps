package com.acconeer.bluetooth.presence.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.acconeer.bluetooth.presence.R;

public class SplashActivity extends AppCompatActivity {
    private static final long DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity_layout);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }, DELAY);
    }

    @Override
    public void onBackPressed() { }
}
