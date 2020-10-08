package com.acconeer.bluetooth.presence.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.acconeer.bluetooth.presence.model.PresenceResult;
import com.acconeer.bluetooth.presence.model.RadarParameters;
import com.acconeer.bluetooth.presence.viewmodels.DeviceViewModel;
import com.acconeer.bluetooth.presence.views.ConnectionButton;
import com.acconeer.bluetooth.presence.views.PresenceView;
import com.acconeer.bluetooth.presence.R;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {
    private FragmentSwitcher switcher;
    private DeviceViewModel deviceViewModel;

    @BindView(R.id.presence_view)
    PresenceView presenceView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.connectionButton)
    ConnectionButton connectionButton;
    @BindView(R.id.distance) TextView distance;
    @BindView(R.id.presence_score) TextView presenceScore;

    private static Boolean previousConnectionState = null; //TODO: Dirty hack
    private DecimalFormat format = new DecimalFormat("#.#");

    public MainFragment() { }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private void setupObservers() {
        deviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
        deviceViewModel.getDeviceName().observe(this, this::onDeviceChange);
        deviceViewModel.getParameters().observe(this, this::onParamsChange);
        deviceViewModel.getResult().observe(this, this::onResultChange);
        deviceViewModel.getConnectionState().observe(this, current -> {
            // If we went from connected to disconnected
            Log.d("CS", "Previous: " + previousConnectionState + ", current: " + current);
            if (previousConnectionState != null && previousConnectionState && !current) {
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.disconnected),
                        Toast.LENGTH_SHORT).show();
            }

            previousConnectionState = current;
        });
        deviceViewModel.getNumZones().observe(this, presenceView::setNumZones);
        deviceViewModel.getDisplayDistance().observe(this, v -> distance
                .setVisibility(v ? View.VISIBLE : View.GONE));
    }

    private void onResultChange(PresenceResult radarResult) {
        if (radarResult == null) {
            distance.setText(getResources().getString(R.string.distance,
                    getActivity().getString(R.string.no_distance_indicator)));
            presenceView.setSection(PresenceView.NONE);
            presenceScore.setText(getResources().getString(R.string.presence_score,
                    getActivity().getString(R.string.no_distance_indicator)));
        } else {
            distance.setText(getResources().getString(R.string.distance,
                    format.format(radarResult.distance)));
            presenceScore.setText(getResources().getString(R.string.presence_score,
                    format.format(radarResult.presenceScore)));
            if (radarResult.presenceDetected) {
                presenceView.setLevel((int) radarResult.distance);
            } else {
                presenceView.setSection(PresenceView.NONE);
            }
        }
    }

    private void onParamsChange(RadarParameters parameters) {
        presenceView.setStart((int) parameters.rangeStart);
        presenceView.setLength((int) parameters.rangeLength);
    }

    private void onDeviceChange(String name) {
        if (name == null) {
            connectionButton.setDisconnected();
        } else {
            connectionButton.setConnected(name);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, view);

        toolbar.setTitle(R.string.presence_detection);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        connectionButton.setConnectAction(v -> switcher.setSettings());
        connectionButton.setDisconnected();

        setupObservers();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentSwitcher) {
            switcher = (FragmentSwitcher) context;
        } else {
            throw new IllegalArgumentException("The containing Activity must implement FragmentSwitcher!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                switcher.setSettings();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
