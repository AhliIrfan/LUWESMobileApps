package com.example.luwesmobileapps.ui.devicepage;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputFilter;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.filter.InputFilterIP;
import com.example.luwesmobileapps.filter.InputFilterMinMax;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DevicePageFragment extends Fragment {
    private SharedViewModel DeviceViewModel;
    private fragmentListener listener;
    //Title Expandable View Button//
    private Button DeviceInfo;
    private Button RecordHistory;
    private Button RealTime;
    private Button Setting;
    private Button Download;
    //Card Container View//
    private CardView DeviceInfoCard;
    private CardView RecordHistoryCard;
    private CardView RealTimeCard;
    private CardView SettingCard;
    private CardView DownloadCard;
    //Device Info Group//
    private LinearLayout DeviceInfoLayout;
    private EditText DeviceName;
    private EditText DeviceModel;
    private EditText FirmwareVersion;
    private EditText MACAddress;
    //Record History Group//
    private LinearLayout RecordHistoryLayout;
    private EditText FirstRecordedDate;
    private EditText LastRecordedDate;
    //Real Time Group//
    private LinearLayout RealTimeLayout;
    private EditText DeviceDateTime;
    private EditText WaterLevel;
    private EditText DeviceBattery;
    private EditText RecordStatus;
    private EditText InternetConnectionStatus;
    private Button Listen;
    private Button TimeSync;
    //Setting Group//
    private LinearLayout SettingLayout;
    private EditText SiteName;
    private EditText IPAddress;
    private EditText Port;
    private EditText ZeroValue;
    private EditText Offset;
    private EditText RecordInterval;
    private TextInputLayout SettingOptContainer;
    private AutoCompleteTextView SettingOpt;
    private Spinner IntervalOpt;
    private Button SetSetting;
    //Download Group//
    private LinearLayout DownloadLayout;
    private EditText StartDate;
    private EditText EndDate;
    private Button StartDownload;

    static int sDay;
    static int sMonth;
    static int sYear;

    static int eDay;
    static int eMonth;
    static int eYear;

    public interface fragmentListener{
        void BTSend(String string);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof fragmentListener) {
            listener = (fragmentListener) context;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_devicepage, container, false);
        //Title Expandable View Button//
        DeviceInfo = v.findViewById(R.id.DeviceInfoTitle);
        RecordHistory = v.findViewById(R.id.RecordHistoryTitle);
        RealTime = v.findViewById(R.id.RealTimeTitle);
        Setting = v.findViewById(R.id.SettingParameterTitle);
        Download = v.findViewById(R.id.DataLogDownloadTitle);
        //Card Container View//
        DeviceInfoCard = v.findViewById(R.id.DeviceInfoCard);
        RecordHistoryCard = v.findViewById(R.id.RecordHistoryCard);
        RealTimeCard = v.findViewById(R.id.RealTimeCard);
        SettingCard = v.findViewById(R.id.SettingParameterCard);
        DownloadCard = v.findViewById(R.id.DataLogDownloadCard);
        //Device Info Group//
        DeviceInfoLayout = v.findViewById(R.id.DeviceInfoGroup);
        DeviceName = v.findViewById(R.id.DeviceName);
        DeviceModel = v.findViewById(R.id.DeviceModel);
        FirmwareVersion = v.findViewById(R.id.FirmwareVersion);
        MACAddress = v.findViewById(R.id.MACAddress);
        //Record History Group//
        RecordHistoryLayout = v.findViewById(R.id.RecordHistoryGroup);
        FirstRecordedDate = v.findViewById(R.id.FirstRecordDate);
        LastRecordedDate = v.findViewById(R.id.LastRecordedDate);
        //Real Time Group//
        RealTimeLayout = v.findViewById(R.id.RealTimeGroup);
        DeviceDateTime = v.findViewById(R.id.DateTime);
        WaterLevel = v.findViewById(R.id.LevelRange);
        DeviceBattery = v.findViewById(R.id.Battery);
        RecordStatus = v.findViewById(R.id.Record);
        InternetConnectionStatus = v.findViewById(R.id.InternetConnection);
        Listen = v.findViewById(R.id.realtimebutton);
        TimeSync = v.findViewById(R.id.timesyncbutton);
        //Setting Group//
        SettingLayout = v.findViewById(R.id.SettingParameterGroup);
        SiteName = v.findViewById(R.id.SiteName);
        IPAddress = v.findViewById(R.id.IPAddress);
        Port = v.findViewById(R.id.Port);
        ZeroValue = v.findViewById(R.id.ZeroValue);
        Offset = v.findViewById(R.id.SensorOffset);
        RecordInterval = v.findViewById(R.id.Interval);
        SettingOptContainer = v.findViewById(R.id.spinner2);
        SettingOpt = v.findViewById(R.id.spinner2content);

        String[] Options = new String[]{
                "Site Name","IP Address and Port","Sensor Offset and Zero Values", "Record Interval"};

        ArrayAdapter<String> SettingAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.setting_option,Options
        );

        IntervalOpt = v.findViewById(R.id.spinner3);
        SetSetting = v.findViewById(R.id.settingButton);
        //Download Group//
        DownloadLayout = v.findViewById(R.id.DataLogDownloadGroup);
        StartDate = v.findViewById(R.id.StartDate);
        EndDate = v.findViewById(R.id.EndDate);
        StartDownload = v.findViewById(R.id.downloadButton);
    //Title Card Opener//
        DeviceInfo.setOnClickListener(view -> {
            if(DeviceInfoLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(DeviceInfoCard, new AutoTransition());
                DeviceInfoLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(DeviceInfoCard, new AutoTransition());
                DeviceInfoLayout.setVisibility(View.VISIBLE);
            }
        });
        RecordHistory.setOnClickListener(view -> {
            if(RecordHistoryLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(RecordHistoryCard, new AutoTransition());
                RecordHistoryLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(RecordHistoryCard, new AutoTransition());
                RecordHistoryLayout.setVisibility(View.VISIBLE);
            }
        });
        RealTime.setOnClickListener(view -> {
            if(RealTimeLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
                RealTimeLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
                RealTimeLayout.setVisibility(View.VISIBLE);
            }
        });
        Setting.setOnClickListener(view -> {
            if(SettingLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                SettingLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                SettingLayout.setVisibility(View.VISIBLE);
                listener.BTSend("LWST,7000000#\r\n");
            }
        });
        Download.setOnClickListener(view -> {
            if(DownloadLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(DownloadCard, new AutoTransition());
                DownloadLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(DownloadCard, new AutoTransition());
                DownloadLayout.setVisibility(View.VISIBLE);
            }
        });
        //Setting Input Handler//
        SettingOpt.setAdapter(SettingAdapter);
        SettingOpt.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i){
                case 0:
                    SiteName.setEnabled(true);
                    IPAddress.setEnabled(false);
                    Port.setEnabled(false);
                    Offset.setEnabled(false);
                    RecordInterval.setEnabled(false);
                    SetSetting.setEnabled(true);
                    break;
                case 1:
                    SiteName.setEnabled(false);
                    IPAddress.setEnabled(true);
                    Port.setEnabled(true);
                    Offset.setEnabled(false);
                    RecordInterval.setEnabled(false);
                    SetSetting.setEnabled(true);
                    break;
                case 2:
                    SiteName.setEnabled(false);
                    IPAddress.setEnabled(false);
                    Port.setEnabled(false);
                    Offset.setEnabled(true);
                    RecordInterval.setEnabled(false);
                    SetSetting.setEnabled(true);
                    break;
                case 3:
                    SiteName.setEnabled(false);
                    IPAddress.setEnabled(false);
                    Port.setEnabled(false);
                    Offset.setEnabled(false);
                    RecordInterval.setEnabled(true);
                    SetSetting.setEnabled(true);
                    break;
            }
        });

        IPAddress.setFilters(new InputFilter[]{new InputFilterIP()});

        IntervalOpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()){
                    case "Minutes":
                        RecordInterval.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "59")});
                        break;
                    case "Hours":
                        RecordInterval.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "24")});
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SetSetting.setOnClickListener(view -> {
            String text = String.valueOf(SettingOpt.getText());
            if ("Site Name".equals(text)) {
                if (SiteName.getText().toString().trim().length() > 4 && SiteName.getText().toString().trim().length() < 21) {
                    listener.BTSend(("LWST,7780000#" + SiteName.getText().toString().trim()));
                } else {
                    SiteName.setError("Site Name must be 5-20 character");
                }
            } else if ("IP Address and Port".equals(text)) {
                if (IPAddress.getText().toString().trim().length() != 0 && Port.getText().toString().trim().length() != 0) {
                    listener.BTSend(("LWST,SIP0000#" + IPAddress.getText().toString().trim() + "#" + Port.getText().toString().trim()));
                } else if (IPAddress.getText().toString().trim().length() < 1) {
                    IPAddress.setError("This field cannot be empty");
                } else if (Port.getText().toString().trim().length() < 1) {
                    Port.setError("This field cannot be empty");
                }
            } else if ("Sensor Offset and Zero Values".equals(text)) {
                if(Offset.getText().toString().trim().length() != 0){
                    listener.BTSend(("LWST,7100000#" + Offset.getText().toString().trim()));
                } else{
                    Offset.setError("This field cannot be empty");
                }
            } else if ("Record Interval".equals(text)) {
                int interval =0;
                if(RecordInterval.getText().toString().trim().length() != 0) {
                    if(IntervalOpt.getSelectedItemPosition()==0)
                        interval = 60 * Integer.parseInt(String.valueOf(RecordInterval.getText()));
                    else
                        interval = 3600 * Integer.parseInt(String.valueOf(RecordInterval.getText()));
                    listener.BTSend(("LWST,7220000#" + interval));
                } else{
                    RecordInterval.setError("This field cannot be empty");
                }
            }
            DeviceViewModel.setSettingStatus(true);
        });

        //Real Time Input Handler//
        Listen.setOnClickListener(view -> {
            if((Listen.getText().toString()).equalsIgnoreCase("Listen")){
                listener.BTSend("LWRT,\r\n");
                DeviceViewModel.setRealTimeStatus(true);
            }
            else if((Listen.getText().toString()).equalsIgnoreCase("Stop")){
                listener.BTSend("LWST,7000000#\r\n");
            }
        });

        TimeSync.setOnClickListener(view -> {
            String TimeSynchronize = new SimpleDateFormat("yy/MM/dd,HH:mm:ss").format(new Date());
            String TimeZone = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                TimeZone = new SimpleDateFormat("X").format(new Date());
            }
            else{
                TimeZone = new SimpleDateFormat("Z").format(new Date());
            }
            int TimeZoneInt = Integer.parseInt(TimeZone) * 4;
            if(TimeZoneInt>0)
                listener.BTSend("LWTS\""+ TimeSynchronize+"+"+TimeZoneInt+"\"\r\n");
            else
                listener.BTSend("LWTS\""+ TimeSynchronize+TimeZoneInt+"\"\r\n");
            DeviceViewModel.setSyncStatus(false);
        });



        //Download Input Handler//
        StartDate.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            final int mYear, mMonth, mDay;
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),R.style.MyDateTimePickerDialogTheme,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date DateToCompare = null;
                            try {
                                DateToCompare = sdf.parse((dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Date DateToCompare2 = null;
                            if (FirstRecordedDate.getText().toString().length()!=0
                                    ||!FirstRecordedDate.getText().toString().trim().equals("No Records")) {
                                try {
                                    DateToCompare2 = sdf.parse(FirstRecordedDate.getText().toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                DateToCompare2 = new Date();
                            }
                            Date DateToCompare3 = null;
                            if (LastRecordedDate.getText().toString().length()!=0
                                    ||!LastRecordedDate.getText().toString().trim().equals("No Records")) {
                                try {
                                    DateToCompare3 = sdf.parse(LastRecordedDate.getText().toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                DateToCompare3 = new Date();
                            }
                            if (DateToCompare.getTime() > DateToCompare3.getTime()) {
                                StartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare3));
                                String[] SplitString = StartDate.getText().toString().split("/");
                                sYear = Integer.parseInt(SplitString[2]);
                                sMonth = Integer.parseInt(SplitString[1]);
                                sDay = Integer.parseInt(SplitString[0]);
                            } else if (DateToCompare.getTime() < DateToCompare2.getTime()) {
                                StartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare2));
                                String[] SplitString = StartDate.getText().toString().split("/");
                                sYear = Integer.parseInt(SplitString[2]);
                                sMonth = Integer.parseInt(SplitString[1]);
                                sDay = Integer.parseInt(SplitString[0]);
                            } else {
                                StartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare));
                                sYear = year;
                                sMonth = monthOfYear + 1;
                                sDay = dayOfMonth;
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        EndDate.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            final int mYear, mMonth, mDay;
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),R.style.MyDateTimePickerDialogTheme,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date DateToCompare = null;
                            try {
                                DateToCompare = sdf.parse((dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Date DateToCompare2 = null;
                            if (FirstRecordedDate.getText().toString().trim().length()!=0||
                                    !FirstRecordedDate.getText().toString().trim().equals("No Records")) {
                                try {
                                    DateToCompare2 = sdf.parse(FirstRecordedDate.getText().toString().trim());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                DateToCompare2 = new Date();
                            }
                            Date DateToCompare3 = null;
                            if (LastRecordedDate.getText().toString().length()!=0||
                                    !LastRecordedDate.getText().toString().trim().equals("No Records")) {
                                try {
                                    DateToCompare3 = sdf.parse(LastRecordedDate.getText().toString().trim());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                DateToCompare3 = new Date();
                            }
                            if (DateToCompare.getTime() > DateToCompare3.getTime()) {
                                EndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare3));
                                String[] SplitString = StartDate.getText().toString().split("/");
                                sYear = Integer.parseInt(SplitString[2]);
                                sMonth = Integer.parseInt(SplitString[1]);
                                sDay = Integer.parseInt(SplitString[0]);
                            } else if (DateToCompare.getTime() < DateToCompare2.getTime()) {
                                EndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare2));
                                String[] SplitString = StartDate.getText().toString().split("/");
                                sYear = Integer.parseInt(SplitString[2]);
                                sMonth = Integer.parseInt(SplitString[1]);
                                sDay = Integer.parseInt(SplitString[0]);
                            } else {
                            EndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare));
                            eYear = year;
                            eMonth = monthOfYear + 1;
                            eDay = dayOfMonth;
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        StartDownload.setOnClickListener(view -> {

        });

        //View Model Callbacks Response//
        DeviceViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        DeviceViewModel.getSiteName().observe(getViewLifecycleOwner(), s -> {
            SiteName.setText(s);
            DeviceName.setText(s);
        });
        DeviceViewModel.getDeviceModel().observe(getViewLifecycleOwner(), s -> DeviceModel.setText(s));
        DeviceViewModel.getFirmWareVersion().observe(getViewLifecycleOwner(), s -> FirmwareVersion.setText(s));
        DeviceViewModel.getMACAddress().observe(getViewLifecycleOwner(), s -> MACAddress.setText(s));
        DeviceViewModel.getDeviceDateTime().observe(getViewLifecycleOwner(), s -> DeviceDateTime.setText(s));
        DeviceViewModel.getFirstRecord().observe(getViewLifecycleOwner(), s -> FirstRecordedDate.setText(s));
        DeviceViewModel.getLastRecord().observe(getViewLifecycleOwner(), s -> LastRecordedDate.setText(s));
        DeviceViewModel.getWaterLevel().observe(getViewLifecycleOwner(), s -> WaterLevel.setText(s));
        DeviceViewModel.getDeviceBattery().observe(getViewLifecycleOwner(), s -> DeviceBattery.setText(s));
        DeviceViewModel.getIPAddress().observe(getViewLifecycleOwner(), s -> IPAddress.setText(s));
        DeviceViewModel.getPort().observe(getViewLifecycleOwner(), s -> Port.setText(s));
        DeviceViewModel.getSensorZeroValues().observe(getViewLifecycleOwner(), s -> ZeroValue.setText(s));
        DeviceViewModel.getSensorOffset().observe(getViewLifecycleOwner(), s -> Offset.setText(s));
        DeviceViewModel.getRecordInterval().observe(getViewLifecycleOwner(), s -> {
            int intervalRec = 0;
            try {
                intervalRec = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                intervalRec = 0;
            }
            if(intervalRec>0) {
                if (intervalRec % 3600 == 0) {
                    IntervalOpt.setSelection(1);
                    RecordInterval.setText(String.valueOf(intervalRec / 3600));
                } else {
                    IntervalOpt.setSelection(0);
                    RecordInterval.setText(String.valueOf(intervalRec / 60));
                }
            }
            else{
                RecordInterval.setText(s);
            }
        });
        DeviceViewModel.getRecordStatus().observe(getViewLifecycleOwner(), s -> RecordStatus.setText(s));
        DeviceViewModel.getInternetConnectionStatus().observe(getViewLifecycleOwner(), s -> InternetConnectionStatus.setText(s));
        DeviceViewModel.getSettingStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(!aBoolean) {
                Toast.makeText(getActivity(), "Setting Success", Toast.LENGTH_SHORT).show();
                SetSetting.setEnabled(true);
            }else{
                SetSetting.setEnabled(false);
            }
        });
        DeviceViewModel.getRealTimeStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                Listen.setText("Stop");
            }
            else{
                Listen.setText("Listen");
            }
        });
        DeviceViewModel.getSyncStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean)
                Toast.makeText(getActivity(), "Time Synchronization Success", Toast.LENGTH_SHORT).show();
        });
        DeviceViewModel.getFilePermission().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                StartDownload.setEnabled(true);
            }else{
                StartDownload.setEnabled(false);
            }
        });
        DeviceViewModel.getConnectStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(!aBoolean){
                getActivity().onBackPressed();
            }
        });

        return v;
    }

}