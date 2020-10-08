package com.acconeer.bluetooth.distance.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acconeer.bluetooth.distance.viewmodels.DeviceSetLiveData;
import com.acconeer.bluetooth.distance.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class FoundDeviceAdapter extends RecyclerView.Adapter<FoundDeviceAdapter.DeviceViewHolder> {
    private List<BluetoothDevice> devices = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(BluetoothDevice device);
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView, macView;
        public View container;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView;
            nameView = itemView.findViewById(R.id.device_name);
            macView = itemView.findViewById(R.id.device_mac);
        }
    }

    public void clear() {
        devices.clear();

        notifyDataSetChanged();
    }

    public FoundDeviceAdapter(Fragment fragment, DeviceSetLiveData liveData,
                              OnItemClickListener itemClickListener) {
        liveData.observe(fragment, devices -> {
            this.devices.clear(); //Not that expensive for few devices
            this.devices.addAll(devices);

            notifyDataSetChanged();
        });

        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_device_item, parent, false);

        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        holder.container.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClicked(device);
            }
        });
        holder.nameView.setText(device.getName());
        holder.macView.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
