package com.acconeer.bluetooth.distance.views.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.acconeer.bluetooth.distance.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.TwoStatePreference;

public class StartPreference extends TwoStatePreference {
    private OnClickListener clickListener;

    private boolean active;

    private ProgressBar progressBar;

    public interface OnClickListener {
        void onClick(boolean active);
    }

    public StartPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.start_pref_layout);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.preferences_StartPreference, 0, 0);

        try {
            setSummaryOn(a.getString(R.styleable.preferences_StartPreference_summaryOn));
            setSummaryOff(a.getString(R.styleable.preferences_StartPreference_summaryOff));
            active = a.getBoolean(R.styleable.preferences_StartPreference_active, false);
        } finally {
            a.recycle();
        }

        setPersistent(false);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        progressBar = (ProgressBar) holder.findViewById(R.id.progress_bar);

//        syncProgressBar();
    }

    @Override
    protected void performClick(View view) {
        super.performClick(view);

        active = !active;

//        syncProgressBar();

        if (clickListener != null) {
            clickListener.onClick(active);
        }
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        if (disableDependent) {
            active = false;
            syncProgressBar();
        }
    }

    private void syncProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }
}
