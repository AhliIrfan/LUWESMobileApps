package com.example.luwesmobileapps.ui.dialog;

import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.service.BTService;
import com.example.luwesmobileapps.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Method;


public class BTScanDialog extends AppCompatDialogFragment {

    private fragmentListener listener;
    private LinearLayout ScanTitle;
    private Button dismiss;
    private Button scan;
    private ProgressBar progressBar;
    private BTViewAdapter adapter;
    private SharedViewModel DeviceViewModel;
    @SuppressLint("MissingPermission")
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bt_scan_dialog, null);

        builder.setView(view);

        RecyclerView recyclerView = view.findViewById(R.id.deviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        adapter = new BTViewAdapter();
        recyclerView.setAdapter(adapter);

        ScanTitle = view.findViewById(R.id.scanTitle);
        dismiss = view.findViewById(R.id.dismissbutton);
        scan = view.findViewById(R.id.scanbutton);
        progressBar = view.findViewById(R.id.scanProgress);



//        final ArrayList<BluetoothDevice> btDevice = new ArrayList();
//        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices=BluetoothAdapter.getDefaultAdapter().getBondedDevices();
//        if (pairedDevices.size() > 0)
//        {
//            for (BluetoothDevice device : pairedDevices)
//            {
//                btDevice.add(device);
//            }
//        }
//
//        adapter.addDeviceList(btDevice);

        dismiss.setOnClickListener(view1 -> {
            listener.BTStopScan();
            dismiss();
        });

        scan.setOnClickListener(view12 -> {
            adapter.clearDeviceList();
            listener.BTStartScan();
            Log.d("TAG", "onCreateDialog: scanpressed");
        });

        adapter.setOnItemClickListener(device -> {
            listener.BTStopScan();
            Intent BTServiceIntent = new Intent(getActivity(), BTService.class);
            BTServiceIntent.putExtra("Device Input", device);
            ContextCompat.startForegroundService(getActivity(), BTServiceIntent);
            dismiss();
        });
        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void AddScannedDevice(BluetoothDevice device){
        adapter.addDevice(device);
    }

    public void ScanButton(int visibility, Boolean status){
        TransitionManager.beginDelayedTransition(ScanTitle,new AutoTransition());
        progressBar.setVisibility(visibility);
        scan.setEnabled(status);
    }

    public interface fragmentListener{
        void BTStartScan();
        void BTStopScan();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BTScanDialog.fragmentListener) {
            listener = (BTScanDialog.fragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement fragment listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener =null;
    }
}
