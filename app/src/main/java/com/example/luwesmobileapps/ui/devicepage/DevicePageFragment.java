package com.example.luwesmobileapps.ui.devicepage;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.SharedViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class DevicePageFragment extends Fragment {

    private SharedViewModel SharedData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_devicepage, container, false);
        //Title Expandable View Button//
        Button DeviceInfo = v.findViewById(R.id.DeviceInfoTitle);
        Button RecordHistory = v.findViewById(R.id.RecordHistoryTitle);
        Button RealTime = v.findViewById(R.id.RealTimeTitle);
        Button Setting = v.findViewById(R.id.SettingParameterTitle);
        Button Download = v.findViewById(R.id.DataLogDownloadTitle);
        //Card Container View//
        CardView DeviceInfoCard = v.findViewById(R.id.DeviceInfoCard);
        CardView RecordHistoryCard = v.findViewById(R.id.RecordHistoryCard);
        CardView RealTimeCard = v.findViewById(R.id.RealTimeCard);
        CardView SettingCard = v.findViewById(R.id.SettingParameterCard);
        CardView DownloadCard = v.findViewById(R.id.DataLogDownloadCard);
        //Device Info Group//
        LinearLayout DeviceInfoLayout = v.findViewById(R.id.DeviceInfoGroup);
        EditText DeviceName = v.findViewById(R.id.DeviceName);
        EditText DeviceModel = v.findViewById(R.id.DeviceModel);
        EditText FirmwareVersion = v.findViewById(R.id.FirmwareVersion);
        EditText MACAddress = v.findViewById(R.id.MACAddress);
        //Record History Group//
        LinearLayout RecordHistoryLayout = v.findViewById(R.id.RecordHistoryGroup);
        EditText FirstRecordedDate = v.findViewById(R.id.FirstRecordDate);
        EditText LastRecordedDate = v.findViewById(R.id.LastRecordedDate);
        //Real Time Group//
        LinearLayout RealTimeLayout = v.findViewById(R.id.RealTimeGroup);
        EditText DeviceDateTime = v.findViewById(R.id.DateTime);
        EditText WaterLevel = v.findViewById(R.id.LevelRange);
        EditText DeviceBattery = v.findViewById(R.id.Battery);
        EditText RecordStatus = v.findViewById(R.id.Record);
        EditText InternetConnectionStatus = v.findViewById(R.id.InternetConnection);
        Button Listen = v.findViewById(R.id.realtimebutton);
        Button TimeSync = v.findViewById(R.id.timesyncbutton);
        //Setting Group//
        LinearLayout SettingLayout = v.findViewById(R.id.SettingParameterGroup);
        EditText SiteName = v.findViewById(R.id.SiteName);
        EditText IPAddress = v.findViewById(R.id.IPAddress);
        EditText Port = v.findViewById(R.id.Port);
        EditText ZeroValue = v.findViewById(R.id.ZeroValue);
        EditText Offset = v.findViewById(R.id.SensorOffset);
        EditText RecordInterval = v.findViewById(R.id.Interval);
//        Spinner SettingOpt = v.findViewById(R.id.spinner2);
        TextInputLayout SettingOptContainer = v.findViewById(R.id.spinner2);
        AutoCompleteTextView SettingOpt = v.findViewById(R.id.spinner2content);

        String[] Options = new String[]{
                "Site Name","IP Address and Port","Sensor Offset and Zero Values", "Record Interval"};

        ArrayAdapter<String> SettingAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.setting_option,Options
        );

        Spinner IntervalOpt = v.findViewById(R.id.spinner3);
        Button SetSetting = v.findViewById(R.id.settingButton);
        //Download Group//
        LinearLayout DownloadLayout = v.findViewById(R.id.DataLogDownloadGroup);
        EditText StartDate = v.findViewById(R.id.StartDate);
        EditText EndDate = v.findViewById(R.id.EndDate);
        Button StartDownload = v.findViewById(R.id.downloadButton);

        DeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DeviceInfoLayout.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(DeviceInfoCard, new AutoTransition());
                    DeviceInfoLayout.setVisibility(View.GONE);
                }
                else{
                    TransitionManager.beginDelayedTransition(DeviceInfoCard, new AutoTransition());
                    DeviceInfoLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        RecordHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RecordHistoryLayout.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(RecordHistoryCard, new AutoTransition());
                    RecordHistoryLayout.setVisibility(View.GONE);
                }
                else{
                    TransitionManager.beginDelayedTransition(RecordHistoryCard, new AutoTransition());
                    RecordHistoryLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        RealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RealTimeLayout.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
                    RealTimeLayout.setVisibility(View.GONE);
                }
                else{
                    TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
                    RealTimeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SettingLayout.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                    SettingLayout.setVisibility(View.GONE);
                }
                else{
                    TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                    SettingLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DownloadLayout.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(DownloadCard, new AutoTransition());
                    DownloadLayout.setVisibility(View.GONE);
                }
                else{
                    TransitionManager.beginDelayedTransition(DownloadCard, new AutoTransition());
                    DownloadLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        SettingOpt.setAdapter(SettingAdapter);
        SettingOpt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        SiteName.setEnabled(true);
                        IPAddress.setEnabled(false);
                        Port.setEnabled(false);
                        ZeroValue.setEnabled(false);
                        RecordInterval.setEnabled(false);
                        SetSetting.setEnabled(true);
                        break;
                    case 1:
                        SiteName.setEnabled(false);
                        IPAddress.setEnabled(true);
                        Port.setEnabled(true);
                        ZeroValue.setEnabled(false);
                        RecordInterval.setEnabled(false);
                        SetSetting.setEnabled(true);
                        break;
                    case 2:
                        SiteName.setEnabled(false);
                        IPAddress.setEnabled(false);
                        Port.setEnabled(false);
                        ZeroValue.setEnabled(true);
                        RecordInterval.setEnabled(false);
                        SetSetting.setEnabled(true);
                        break;
                    case 3:
                        SiteName.setEnabled(false);
                        IPAddress.setEnabled(false);
                        Port.setEnabled(false);
                        ZeroValue.setEnabled(false);
                        RecordInterval.setEnabled(true);
                        SetSetting.setEnabled(true);
                        break;
                }
            }
        });

        return v;
    }

}