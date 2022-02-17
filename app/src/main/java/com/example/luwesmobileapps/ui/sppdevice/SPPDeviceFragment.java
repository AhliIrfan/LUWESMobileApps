package com.example.luwesmobileapps.ui.sppdevice;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.SharedViewModel;

public class SPPDeviceFragment extends Fragment {

    private SharedViewModel SharedData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sppdevice, container, false);
        EditText DeviceName = v.findViewById(R.id.DeviceName);
        EditText DeviceModel = v.findViewById(R.id.DeviceModel);
        EditText FirmwareVersion = v.findViewById(R.id.FirmwareVersion);
        EditText MACAddress = v.findViewById(R.id.MACAddress);
        EditText FirstRecordedDate = v.findViewById(R.id.FirstRecordDate);
        EditText LastRecordedDate = v.findViewById(R.id.LastRecordedDate);
        EditText DeviceDateTime = v.findViewById(R.id.DateTime);
        EditText WaterLevel = v.findViewById(R.id.LevelRange);
        EditText DeviceBattery = v.findViewById(R.id.Battery);
        EditText RecordStatus = v.findViewById(R.id.Record);
        EditText InternetConnectionStatus = v.findViewById(R.id.InternetConnection);
        EditText SiteName = v.findViewById(R.id.SiteName);
        EditText IPAddress = v.findViewById(R.id.IPAddress);
        EditText Port = v.findViewById(R.id.Port);
        EditText ZeroValue = v.findViewById(R.id.ZeroValue);
        EditText Offset = v.findViewById(R.id.SensorOffset);
        EditText RecordInterval = v.findViewById(R.id.Interval);
        EditText StartDate = v.findViewById(R.id.StartDate);
        EditText EndDate = v.findViewById(R.id.EndDate);
        Button Listen = v.findViewById(R.id.realtimebutton);
        Button Setting = v.findViewById(R.id.settingButton);
        Button Download = v.findViewById(R.id.downloadButton);
        Spinner SettingOpt = v.findViewById(R.id.spinner2);
        Spinner IntervalOpt = v.findViewById(R.id.spinner3);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedData = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        SharedData.getSiteName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getMACAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getFirmWareVersion().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getDeviceModel().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getDeviceDateTime().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getWaterLevel().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getDeviceBattery().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getIPAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getPort().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getSensorZeroValues().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getSensorOffset().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getRecordInterval().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getRecordStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        SharedData.getInternetConnectionStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
    }
}