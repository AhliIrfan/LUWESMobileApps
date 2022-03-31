package com.example.luwesmobileapps.ui.dialog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luwesmobileapps.R;

import java.util.ArrayList;
import java.util.List;

public class BLEViewAdapter extends RecyclerView.Adapter<BLEViewAdapter.BTViewHolder> {
    private List<BluetoothDevice> myDeviceList = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public BTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_device_listview,parent,false);
        return new BTViewHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BTViewHolder holder, int position) {
        BluetoothDevice selectedDevice = myDeviceList.get(position);
        holder.BLEName.setText(selectedDevice.getName());
        holder.BLEAddress.setText(selectedDevice.getAddress());

    }

    @Override
    public int getItemCount() {
        return myDeviceList.size();
    }

    public void setThisDevice(List<BluetoothDevice> thisDevice) {
        this.myDeviceList = thisDevice;
        notifyDataSetChanged();
    }

    class BTViewHolder extends RecyclerView.ViewHolder{
        private TextView BLEName;
        private TextView BLEAddress;

        public BTViewHolder(@NonNull View itemView) {
            super(itemView);
            BLEName = itemView.findViewById(R.id.serviceName);
            BLEAddress = itemView.findViewById(R.id.serviceUUID);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(myDeviceList.get(position));
                }
            });
        }
    }

    public void addDevice(BluetoothDevice thisDevice) {
        this.myDeviceList.add(thisDevice);
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }

    public void setOnItemClickListener(BLEViewAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void clearDeviceList(){
        this.myDeviceList.clear();
    }
}
