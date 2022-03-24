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
import android.widget.TextView;
import android.widget.Toast;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.filter.InputFilterIP;
import com.example.luwesmobileapps.filter.InputFilterMinMax;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DevicePageFragment extends Fragment {
    private SharedViewModel DeviceViewModel;
    private fragmentListener listener;
    //Title Expandable View Button//
    private MaterialButton DeviceInfo;
    private MaterialButton RecordHistory;
//    private Button RealTime;
    private MaterialButton Setting;
    private MaterialButton Download;
    //Card Container View//
    private CardView DeviceInfoCard;
    private CardView RecordHistoryCard;
//    private CardView RealTimeCard;
    private CardView SettingCard;
    private CardView DownloadCard;
    //Device Info Group//
    private LinearLayout DeviceInfoLayout;
    private TextView DeviceName;
    private TextView DeviceModel;
    private TextView FirmwareVersion;
    private TextView MACAddress;
    //Record History Group//
    private LinearLayout RecordHistoryLayout;
    private TextView FirstRecordedDate;
    private TextView LastRecordedDate;
//    //Real Time Group//
    //Setting Group//
    private LinearLayout SettingLayout;
    private LinearLayout SettingInputLayoutText;
    private LinearLayout SettingInputLayoutText2;
    private LinearLayout SettingInputLayoutInterval;
    private TextView SiteName;
    private TextView IPAddress;
    private TextView Port;
    private TextView ZeroValue;
    private TextView Offset;
    private TextView RecordInterval;
    private AutoCompleteTextView SettingOpt;
    private TextView IntervalOpt;
    private EditText SettingInput;
    private EditText SettingInput2;
    private TextView SettingInputTitle;
    private TextView SettingInputTitle2;
    private EditText RecordIntervalInput;
    private Spinner IntervalOptInput;
    private MaterialButton SetSetting;
    private MaterialButton TimeSync;
    //Download Group//
    private LinearLayout DownloadLayout;
    private EditText StartDate;
    private EditText EndDate;
    private MaterialButton StartDownload;

    static int sDay;
    static int sMonth;
    static int sYear;

    static int eDay;
    static int eMonth;
    static int eYear;

    public interface fragmentListener{
        void BTSend(String string);
        void BTStartDownload(int downloadLength, int startDoY, int startYear);
        void BLESend(String string);
        void BLEStartDownload(int downloadLength, int startDoY, int startYear);
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
//        RealTime = v.findViewById(R.id.RealTimeTitle);
        Setting = v.findViewById(R.id.SettingParameterTitle);
        Download = v.findViewById(R.id.DataLogDownloadTitle);
        //Card Container View//
        DeviceInfoCard = v.findViewById(R.id.DeviceInfoCard);
        RecordHistoryCard = v.findViewById(R.id.RecordHistoryCard);
//        RealTimeCard = v.findViewById(R.id.RealTimeCard);
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
        //Setting Group//
        SettingLayout = v.findViewById(R.id.SettingParameterGroup);
        SettingInputLayoutText = v.findViewById(R.id.SettingInputText);
        SettingInputLayoutText2 = v.findViewById(R.id.SettingInputText2);
        SettingInputLayoutInterval = v.findViewById(R.id.SettingInputInterval);
        SiteName = v.findViewById(R.id.SiteName);
        IPAddress = v.findViewById(R.id.IPAddress);
        Port = v.findViewById(R.id.Port);
        ZeroValue = v.findViewById(R.id.ZeroValue);
        Offset = v.findViewById(R.id.SensorOffset);
        RecordInterval = v.findViewById(R.id.Interval);
        SettingOpt = v.findViewById(R.id.spinner2content);

        String[] Options = new String[]{
                "Site Name","IP Address and Port","Sensor Offset and Zero Values", "Record Interval"};

        ArrayAdapter<String> SettingAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.setting_option,Options
        );

        IntervalOpt = v.findViewById(R.id.spinner3);
        IntervalOptInput = v.findViewById(R.id.spinner4);
        RecordIntervalInput = v.findViewById(R.id.IntervalInput);
        SettingInputTitle = v.findViewById(R.id.InputSettingTitle);
        SettingInputTitle2 = v.findViewById(R.id.InputSettingTitle2);
        SettingInput2 = v.findViewById(R.id.InputSetting2);
        SettingInput = v.findViewById(R.id.InputSetting);
        SetSetting = v.findViewById(R.id.settingButton);
        TimeSync = v.findViewById(R.id.timesyncbutton);
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
//        RealTime.setOnClickListener(view -> {
//            if(RealTimeLayout.getVisibility()==View.VISIBLE){
//                TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
//                RealTimeLayout.setVisibility(View.GONE);
//            }
//            else{
//                TransitionManager.beginDelayedTransition(RealTimeCard, new AutoTransition());
//                RealTimeLayout.setVisibility(View.VISIBLE);
//            }
//        });
        Setting.setOnClickListener(view -> {
            if(SettingLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                SettingLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                SettingLayout.setVisibility(View.VISIBLE);
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWST,7000000#\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWST,7000000#\r\n");
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
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("Site Name");
                    SetSetting.setEnabled(true);
                    break;
                case 1:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.VISIBLE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("IP Address");
                    SettingInputTitle2.setText("Port");
                    SetSetting.setEnabled(true);
                    break;
                case 2:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("Offset");
                    SetSetting.setEnabled(true);
                    break;
                case 3:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.GONE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.VISIBLE);
                    SetSetting.setEnabled(true);
                    break;
            }
        });

        IPAddress.setFilters(new InputFilter[]{new InputFilterIP()});

        IntervalOptInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()){
                    case "Minutes":
                        RecordIntervalInput.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "59")});
                        break;
                    case "Hours":
                        RecordIntervalInput.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "24")});
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
                if (SettingInput.getText().toString().trim().length() > 4 && SettingInput.getText().toString().trim().length() < 21) {
                    if(DeviceViewModel.getConnectStatus().getValue()==1)
                        listener.BTSend(("LWST,7780000#" + SettingInput.getText().toString().trim()));
                    else if(DeviceViewModel.getConnectStatus().getValue()==2)
                        listener.BLESend(("LWST,7780000#" + SettingInput.getText().toString().trim()));
                    DeviceViewModel.setSettingStatus(true);
                } else {
                  Toast.makeText(getContext(),"Site Name must be 5-20 character",Toast.LENGTH_SHORT).show();
                }
            } else if ("IP Address and Port".equals(text)) {
                if (SettingInput.getText().toString().trim().length() != 0 && SettingInput2.getText().toString().trim().length() != 0) {
                    if(DeviceViewModel.getConnectStatus().getValue()==1)
                        listener.BTSend(("LWST,SIP0000#" + SettingInput.getText().toString().trim() + "#" + SettingInput2.getText().toString().trim()));
                    else if(DeviceViewModel.getConnectStatus().getValue()==2)
                        listener.BLESend(("LWST,SIP0000#" + SettingInput.getText().toString().trim() + "#" + SettingInput2.getText().toString().trim()));
                    DeviceViewModel.setSettingStatus(true);
                 }else if (IPAddress.getText().toString().trim().length() < 1) {
                    Toast.makeText(getContext(),"Please fill the IP address field",Toast.LENGTH_SHORT).show();
                } else if (Port.getText().toString().trim().length() < 1) {
                    Toast.makeText(getContext(),"Please fill the port field",Toast.LENGTH_SHORT).show();
                }
            } else if ("Sensor Offset and Zero Values".equals(text)) {
                if(SettingInput.getText().toString().trim().length() != 0){
                    if(DeviceViewModel.getConnectStatus().getValue()==1)
                        listener.BTSend(("LWST,7100000#" + SettingInput.getText().toString().trim()));
                    else if(DeviceViewModel.getConnectStatus().getValue()==2)
                        listener.BLESend(("LWST,7100000#" + SettingInput.getText().toString().trim()));
                    DeviceViewModel.setSettingStatus(true);
                } else{
                    Toast.makeText(getContext(),"Please fill the offset value field",Toast.LENGTH_SHORT).show();
                }
            } else if ("Record Interval".equals(text)) {
                int interval =0;
                if(RecordIntervalInput.getText().toString().trim().length() != 0) {
                    if(IntervalOptInput.getSelectedItemPosition()==0)
                        interval = 60 * Integer.parseInt(String.valueOf(RecordIntervalInput.getText()));
                    else
                        interval = 3600 * Integer.parseInt(String.valueOf(RecordIntervalInput.getText()));

                    if(DeviceViewModel.getConnectStatus().getValue()==1)
                        listener.BTSend(("LWST,7220000#" + interval));
                    else if(DeviceViewModel.getConnectStatus().getValue()==2)
                        listener.BLESend(("LWST,7220000#" + interval));
                    DeviceViewModel.setSettingStatus(true);
                } else{
                    Toast.makeText(getContext(),"Please fill the record interval field",Toast.LENGTH_SHORT).show();
                }
            }
        });

//        //Real Time Input Handler//
//        Listen.setOnClickListener(view -> {
//            if((Listen.getText().toString()).equalsIgnoreCase("Listen")){
//                if(DeviceViewModel.getConnectStatus().getValue()==1)
//                    listener.BTSend("LWRT,\r\n");
//                else if(DeviceViewModel.getConnectStatus().getValue()==2)
//                    listener.BLESend("LWRT,\r\n");
//                DeviceViewModel.setRealTimeStatus(true);
//            }
//            else if((Listen.getText().toString()).equalsIgnoreCase("Stop")){
//                if(DeviceViewModel.getConnectStatus().getValue()==1)
//                    listener.BTSend("LWST,7000000#\r\n");
//                else if(DeviceViewModel.getConnectStatus().getValue()==2)
//                    listener.BLESend("LWST,7000000#\r\n");
//            }
//        });

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
            if(TimeZoneInt>0){
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWTS\""+ TimeSynchronize+"+"+TimeZoneInt+"\"\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWTS\""+ TimeSynchronize+"+"+TimeZoneInt+"\"\r\n");
            }
            else{
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWTS\""+ TimeSynchronize+TimeZoneInt+"\"\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWTS\""+ TimeSynchronize+TimeZoneInt+"\"\r\n");
            }
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
            int DayofTheYear = 0;
            int DownloadLength =0;
            if(StartDate.getText().toString().isEmpty())
                Toast.makeText(getContext(),"Please fill the start date",Toast.LENGTH_SHORT).show();
            else if(EndDate.getText().toString().isEmpty())
                Toast.makeText(getContext(),"Please fill the end date",Toast.LENGTH_SHORT).show();
            else if(FirstRecordedDate==null||LastRecordedDate==null)
                Toast.makeText(getContext(), "There is currently no data to be downloaded", Toast.LENGTH_SHORT).show();
            else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date endDateValue = null;
                try {
                    endDateValue = sdf.parse(EndDate.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date startDateValue = null;
                try {
                    startDateValue = sdf.parse(StartDate.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date startFirstFayOfTheYear = null;
                try {
                    startFirstFayOfTheYear = sdf.parse(1 + "/" + 1 + "/" + sYear);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long diff = endDateValue.getTime() - startDateValue.getTime();
                long diff2 = startDateValue.getTime() - startFirstFayOfTheYear.getTime();
                DownloadLength = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                DayofTheYear = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;

                if (DownloadLength <= 0) {
                    Toast.makeText(getContext(), "Start date must be older than end date", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(DeviceViewModel.getConnectStatus().getValue()==1){
                        listener.BTStartDownload(DownloadLength,DayofTheYear,sYear);
                        listener.BTSend(String.format("LWDL,%02d,%03d,%03d,\r\n",(sYear - 2000),DayofTheYear,DownloadLength));
                    }else if(DeviceViewModel.getConnectStatus().getValue()==2){
                        listener.BLEStartDownload(DownloadLength,DayofTheYear,sYear);
                        listener.BLESend(String.format("LWDL,%02d,%03d,%03d,\r\n",(sYear - 2000),DayofTheYear,DownloadLength));
                    }
                }
                DeviceViewModel.setDownloadStatus(true);
            }
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
        DeviceViewModel.getFirstRecord().observe(getViewLifecycleOwner(), s -> FirstRecordedDate.setText(s));
        DeviceViewModel.getLastRecord().observe(getViewLifecycleOwner(), s -> LastRecordedDate.setText(s));
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
                    IntervalOpt.setText("Hours");
                    RecordInterval.setText(String.valueOf(intervalRec / 3600));
                } else {
                    IntervalOpt.setText("Minutes");
                    RecordInterval.setText(String.valueOf(intervalRec / 60));
                }
            }
            else{
                RecordInterval.setText(s);
            }
        });
        DeviceViewModel.getSettingStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(!aBoolean) {
                Toast.makeText(getContext(), "Setting Success", Toast.LENGTH_SHORT).show();
                SetSetting.setEnabled(true);
            }else{
                SetSetting.setEnabled(false);
            }
        });
        DeviceViewModel.getSyncStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean)
                Toast.makeText(getContext(), "Time Synchronization Success", Toast.LENGTH_SHORT).show();
        });
        DeviceViewModel.getFilePermission().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                StartDownload.setEnabled(true);
            }else{
                StartDownload.setEnabled(false);
            }
        });
        DeviceViewModel.getDownloadStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                StartDownload.setEnabled(false);
            }else{
                StartDownload.setEnabled(true);
            }
        });
        DeviceViewModel.getConnectStatus().observe(getViewLifecycleOwner(), integer -> {
            if(integer==0){
                getActivity().onBackPressed();
            }
        });

        return v;
    }

}