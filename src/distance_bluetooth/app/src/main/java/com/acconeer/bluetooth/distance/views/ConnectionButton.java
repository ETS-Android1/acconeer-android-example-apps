package com.acconeer.bluetooth.distance.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.acconeer.bluetooth.distance.R;

public class ConnectionButton extends LabeledImageButton {
    private Drawable connectedIcon, disconnectedIcon;

    private OnClickListener listener;

    public ConnectionButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        connectedIcon = getResources().getDrawable(R.drawable.ic_bluetooth_connected_black_24dp);
        disconnectedIcon = getResources().getDrawable(R.drawable.ic_bluetooth_disabled_black_24dp);
    }

    public void setConnectAction(OnClickListener listener) {
        this.listener = listener;
    }

    public void setConnected(String deviceName) {
        textView.setText(deviceName);
        imageButton.setImageDrawable(connectedIcon);

        imageButton.setOnClickListener(null);
    }

    public void setDisconnected() {
        textView.setText(R.string.connect);
        imageButton.setImageDrawable(disconnectedIcon);

        imageButton.setOnClickListener(listener);
    }
}
