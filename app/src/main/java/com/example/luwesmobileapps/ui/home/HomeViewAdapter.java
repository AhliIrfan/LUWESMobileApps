package com.example.luwesmobileapps.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;

public class HomeViewAdapter extends ListAdapter<DeviceData ,HomeViewAdapter.BTViewHolder> {
    private OnItemClickListener clickListener;
    private DeviceData LongClickItemHolder;

    protected HomeViewAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<DeviceData> DIFF_CALLBACK = new DiffUtil.ItemCallback<DeviceData>() {
        @Override
        public boolean areItemsTheSame(@NonNull DeviceData oldItem, @NonNull DeviceData newItem) {
            return oldItem.getDeviceAddress().equals(newItem.getDeviceAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DeviceData oldItem, @NonNull DeviceData newItem) {
            return oldItem.getDeviceName().equals(newItem.getDeviceName()) &&
                    oldItem.getLastWaterLevel().equals(newItem.getLastWaterLevel()) &&
                    oldItem.getLastBattery().equals(newItem.getLastBattery()) &&
                    oldItem.getLastConnection().equals(newItem.getLastConnection());
        }
    };

    @NonNull
    @Override
    public BTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_device_listview,parent,false);
        return new BTViewHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BTViewHolder holder, int position) {
        DeviceData selectedDevice = getItem(position);
        holder.DeviceName.setText(selectedDevice.getDeviceName());
        holder.DeviceAddress.setText(selectedDevice.getDeviceAddress());
        if(selectedDevice.getLastWaterLevel()!=null){
            if(!selectedDevice.getLastWaterLevel().equals(""))
                holder.LastWaterLevel.setText(selectedDevice.getLastWaterLevel()+" m");
            else
                holder.LastWaterLevel.setText("Not Measured Yet");

        }
        else
            holder.LastWaterLevel.setText("Not Measured Yet");
        if(selectedDevice.getLastBattery()!=null) {
            if (!selectedDevice.getLastBattery().equals(""))
                holder.LastBatteryLevel.setText(selectedDevice.getLastBattery() + " v");
            else
                holder.LastBatteryLevel.setText("Not Measured Yet");
        }
        else
            holder.LastBatteryLevel.setText("Not Measured Yet");
        holder.LastConnection.setText(selectedDevice.getLastConnection());
        if(selectedDevice.getDeviceModel()!=null) {
            if (selectedDevice.getDeviceModel().equals("Promithevo-U")) {
                holder.IconContainer.setImageResource(R.drawable.ic_promithevo_u);
            } else if (selectedDevice.getDeviceModel().equals("Promithevo-P")) {
                holder.IconContainer.setImageResource(R.drawable.ic_promithevo_p);
            }
        }

    }

    public DeviceData getDevicePosition(int position){
        return getItem(position);
    }


    class BTViewHolder extends RecyclerView.ViewHolder{
        private TextView DeviceName;
        private TextView DeviceAddress;
        private TextView LastWaterLevel;
        private TextView LastBatteryLevel;
        private TextView LastConnection;
        private ImageView IconContainer;


        public BTViewHolder(@NonNull View itemView) {
            super(itemView);
            DeviceName = itemView.findViewById(R.id.deviceName);
            DeviceAddress = itemView.findViewById(R.id.deviceAddress);
            LastWaterLevel = itemView.findViewById(R.id.lastWaterLevel);
            LastBatteryLevel = itemView.findViewById(R.id.lastBattery);
            LastConnection = itemView.findViewById(R.id.lastConnection);
            IconContainer= itemView.findViewById(R.id.IconContainer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (clickListener != null && position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        LongClickItemHolder=getItem(position);
                    }
                    return false;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DeviceData device);
    }

    public DeviceData getLongClickedItem(){
        return LongClickItemHolder;
    }

    public void setOnItemClickListener(HomeViewAdapter.OnItemClickListener listener) {
        this.clickListener = listener;
    }

}
