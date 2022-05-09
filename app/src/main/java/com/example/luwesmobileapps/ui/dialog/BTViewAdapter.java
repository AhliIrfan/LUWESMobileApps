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

public class BTViewAdapter extends RecyclerView.Adapter<BTViewAdapter.BTViewHolder> {
    private List<BluetoothDevice> myDeviceList = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public BTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bt_device_listview,parent,false);
        return new BTViewHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BTViewHolder holder, int position) {
        BluetoothDevice selectedDevice = myDeviceList.get(position);
        holder.BTAddress.setText(selectedDevice.getAddress());
        holder.BTName.setText(selectedDevice.getName());

    }

    @Override
    public int getItemCount() {
        return myDeviceList.size();
    }

//    public void addDeviceList(List<BluetoothDevice> thisDevice) {
//        this.myDeviceList=thisDevice;
//        notifyDataSetChanged();
//    }

    public void addDevice(BluetoothDevice thisDevice) {
        if(!myDeviceList.contains(thisDevice)) {
            this.myDeviceList.add(thisDevice);
            notifyDataSetChanged();
        }
    }

    class BTViewHolder extends RecyclerView.ViewHolder{
        private TextView BTName;
        private TextView BTAddress;

        public BTViewHolder(@NonNull View itemView) {
            super(itemView);
            BTName = itemView.findViewById(R.id.deviceName);
            BTAddress = itemView.findViewById(R.id.deviceAddress);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(myDeviceList.get(position));
                }
            });
        }

    }
    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void clearDeviceList(){
        this.myDeviceList.clear();
    }
}
