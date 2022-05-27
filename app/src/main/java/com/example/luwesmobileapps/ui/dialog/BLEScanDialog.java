package com.example.luwesmobileapps.ui.dialog;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

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
    private BLEViewAdapter adapter;
    private ProgressBar progressBar;
    private Button dismiss;
    private Button scan;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ble_scan_dialog, null);

        builder.setView(view);

        RecyclerView recyclerView = view.findViewById(R.id.deviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        adapter = new BLEViewAdapter();
        recyclerView.setAdapter(adapter);

        progressBar = view.findViewById(R.id.scanProgress);
        scan = view.findViewById(R.id.scanbutton);
        dismiss = view.findViewById(R.id.dismissbutton);

        dismiss.setOnClickListener(view1 -> {
            listener.BLEStopScan();
            listener.dismissScan();
            dismiss();
        });

        scan.setOnClickListener(view12 -> {
            adapter.clearDeviceList();
            listener.BLEStartScan();
        });

        adapter.setOnItemClickListener(new BLEViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice device) {
                listener.BLEStopScan();
                Intent BLEServiceIntent = new Intent(getActivity(), BLEService.class);
                BLEServiceIntent.putExtra("Device Input",device);
                ContextCompat.startForegroundService(requireActivity(),BLEServiceIntent);
                listener.dismissScan();
                dismiss();
            }
        });
        return builder.create();
    }

    public interface fragmentListener{
        void BLEStartScan();
        void BLEStopScan();
        void dismissScan();
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
            throw new RuntimeException(context
                    + " must implement fragment listener");
        }
    }

    public void showProgressbar(boolean status){
        if(status){
            progressBar.setVisibility(View.VISIBLE);
            scan.setEnabled(false);
        }
        else{
            progressBar.setVisibility(View.INVISIBLE);
            scan.setEnabled(true);
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        listener =null;
    }
}
