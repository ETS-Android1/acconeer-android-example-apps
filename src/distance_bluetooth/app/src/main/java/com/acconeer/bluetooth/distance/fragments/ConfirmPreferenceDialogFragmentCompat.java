package com.acconeer.bluetooth.distance.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.views.preferences.ConfirmPreference;

import androidx.preference.PreferenceDialogFragmentCompat;

public class ConfirmPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private ConfirmResult onConfirmResult;

    public interface ConfirmResult {
        void onPositive(PreferenceDialogFragmentCompat dialog);
        default void onNegative(PreferenceDialogFragmentCompat dialog) {}
    }

    public ConfirmPreferenceDialogFragmentCompat(ConfirmResult confirmResult) {
        this.onConfirmResult = confirmResult;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        int padding = (int) getResources().getDimension(R.dimen.internal_padding);
        view.setPadding(2 * padding, padding, padding, padding);

        ConfirmPreference preference = (ConfirmPreference) getPreference();

        TextView text = view.findViewById(R.id.dialog_text);
        text.setText(preference.getDialogText());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            onConfirmResult.onPositive(this);
        } else {
            onConfirmResult.onNegative(this);
        }
    }

    public static ConfirmPreferenceDialogFragmentCompat newInstance(String key, ConfirmResult confirmResult) {
        final ConfirmPreferenceDialogFragmentCompat fragment =
                new ConfirmPreferenceDialogFragmentCompat(confirmResult);
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }
}
