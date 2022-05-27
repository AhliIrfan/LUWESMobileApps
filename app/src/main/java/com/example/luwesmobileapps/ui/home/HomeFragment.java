package com.example.luwesmobileapps.ui.home;

import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;
import com.example.luwesmobileapps.data_layer.SharedData;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.service.BLEService;
import com.example.luwesmobileapps.service.BTService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {
    private fragmentListener listener;
    private HomeViewAdapter adapter;
    private SharedViewModel DeviceViewModel;
    private int deleteReq = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        Type deviceType = new TypeToken<ArrayList<DeviceData>>() {}.getType();

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();

        String jsonString = appSharedPrefs.getString("deviceList", null);

        if(gson.fromJson(jsonString, deviceType)!=null) {
            SharedData.deviceList = gson.fromJson(jsonString, deviceType);
        }

        RecyclerView recyclerView = v.findViewById(R.id.DeviceContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        DeviceViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        registerForContextMenu(recyclerView);

        adapter = new HomeViewAdapter();
        recyclerView.setAdapter(adapter);
        adapter.submitList(SharedData.deviceList);
        adapter.setOnItemClickListener(device -> {
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mStartForResult.launch(enableBtIntent);
                listener.dismissScan();
            }
            if(bluetoothAdapter.isEnabled()) {
                if (DeviceViewModel.getConnectStatus().getValue() != null) {
                    if (DeviceViewModel.getConnectStatus().getValue() == 0) {
                        listener.dismissScan();
                        if (bluetoothAdapter.getRemoteDevice(device.getDeviceAddress()) != null) {
                            if(device.getDeviceConnection()==1){
                                ConnectBT(device);
                            }else if(device.getDeviceConnection()==2){
                                ConnectBLE(device);
                            }
                        }
                    }
                } else {
                    if (bluetoothAdapter.getRemoteDevice(device.getDeviceAddress()) != null) {
                        listener.dismissScan();
                        if(device.getDeviceConnection()==1){
                            ConnectBT(device);
                        }else if(device.getDeviceConnection()==2){
                            ConnectBLE(device);
                        }
                    }
                }
            }
        });

        DeviceViewModel.getDeviceDataChanged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    DeviceData thisDevice = new DeviceData(DeviceViewModel.getMACAddress().getValue());
                    if (!SharedData.deviceList.contains(thisDevice)) {
                        thisDevice.setDeviceName(DeviceViewModel.getSiteName().getValue());
                        thisDevice.setDeviceModel(DeviceViewModel.getDeviceModel().getValue());
                        thisDevice.setDeviceConnection(DeviceViewModel.getConnectStatus().getValue());
                        thisDevice.setLastConnection(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                        SharedData.deviceList.add(thisDevice);
                        adapter.notifyItemInserted(SharedData.deviceList.indexOf(thisDevice));
                    } else {
                        SharedData.deviceList.get(SharedData.deviceList.indexOf(thisDevice)).setDeviceName(DeviceViewModel.getSiteName().getValue());
                        SharedData.deviceList.get(SharedData.deviceList.indexOf(thisDevice)).setLastBattery(DeviceViewModel.getDeviceBattery().getValue()+" | "
                                +DeviceViewModel.getBatteryCapacity().getValue());
                        SharedData.deviceList.get(SharedData.deviceList.indexOf(thisDevice)).setLastWaterLevel(DeviceViewModel.getWaterLevel().getValue());
                        SharedData.deviceList.get(SharedData.deviceList.indexOf(thisDevice)).setDeviceModel(DeviceViewModel.getDeviceModel().getValue());
                        SharedData.deviceList.get(SharedData.deviceList.indexOf(thisDevice)).setLastConnection(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                        adapter.notifyItemChanged(SharedData.deviceList.indexOf(thisDevice));
                    }
                    listener.saveDeviceList();
                    adapter.submitList(SharedData.deviceList);
                    DeviceViewModel.setDeviceDataChanged(false);
                }
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog myDialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Remove Item")
                        .setMessage("Do you want to remove this device from the list?")
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedData.deviceList.remove(adapter.getDevicePosition(viewHolder.getAdapterPosition()));
                                listener.saveDeviceList();
                                deleteReq=1;
                                dialogInterface.dismiss();
                            }
                        })
                        .create();

                myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(deleteReq==0)
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        else
                            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        deleteReq=0;
                    }
                });
                myDialog.show();

            }
        }).attachToRecyclerView(recyclerView);
        return v;
    }

    public interface fragmentListener{
        void saveDeviceList();
        void dismissScan();
    }

    public void ConnectBT(DeviceData device){
        BluetoothDevice thisDevice = bluetoothAdapter.getRemoteDevice(device.getDeviceAddress());
        Intent BTServiceIntent = new Intent(getActivity(), BTService.class);
        BTServiceIntent.putExtra("Device Input", thisDevice);
        ContextCompat.startForegroundService(requireActivity(), BTServiceIntent);
    }

    public void ConnectBLE(DeviceData device){
        BluetoothDevice thisDevice = bluetoothAdapter.getRemoteDevice(device.getDeviceAddress());
        Intent BLEServiceIntent = new Intent(getActivity(), BLEService.class);
        BLEServiceIntent.putExtra("Device Input", thisDevice);
        ContextCompat.startForegroundService(requireActivity(), BLEServiceIntent);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof fragmentListener) {
            listener = (fragmentListener) context;
        } else {
            throw new RuntimeException(context
                    + " must implement fragment listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener =null;
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    // Handle the Intent
                }
            });

}
