package com.example.luwesmobileapps.ui.dialog;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.service.BLEService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BLEScanDialog extends AppCompatDialogFragment {
    private fragmentListener listener;
    private BLEViewAdaper adapter;
    private Button dismiss;
    private Button scan;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ble_scan_dialog, null);

        builder.setView(view);

        RecyclerView recyclerView = view.findViewById(R.id.deviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        adapter = new BLEViewAdaper();
        recyclerView.setAdapter(adapter);

        scan = view.findViewById(R.id.scanbutton);
        dismiss = view.findViewById(R.id.dismissbutton);

        dismiss.setOnClickListener(view1 -> {
            listener.BLEStopScan();
            dismiss();
        });

        scan.setOnClickListener(view12 -> {
            listener.BLEStartScan();
        });

        adapter.setOnItemClickListener(new BLEViewAdaper.OnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice device) {
                listener.BLEStopScan();
                Intent BLEServiceIntent = new Intent(getActivity(), BLEService.class);
                BLEServiceIntent.putExtra("Device Input",device);
                ContextCompat.startForegroundService(getActivity(),BLEServiceIntent);
                dismiss();
            }
        });
        return builder.create();
    }

    public interface fragmentListener{
        void BLEStartScan();
        void BLEStopScan();
    }

    public void AddScannedDevice(BluetoothDevice device){
        adapter.addDevice(device);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BLEScanDialog.fragmentListener) {
            listener = (BLEScanDialog.fragmentListener) context;
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
