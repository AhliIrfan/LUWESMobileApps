package com.example.luwesmobileapps.ui.devicepage;


import android.animation.LayoutTransition;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.renderscript.ScriptGroup;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;
import com.example.luwesmobileapps.data_layer.SharedData;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.filter.InputFilterIP;
import com.example.luwesmobileapps.filter.InputFilterMinMax;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.vdurmont.emoji.EmojiManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DevicePageFragment extends Fragment {
    private RelativeLayout MainLayout;
    private SharedViewModel DeviceViewModel;
    private fragmentListener listener;
    //Title Expandable View Button//
    private MaterialButton DeviceInfo;
    private MaterialButton RecordHistory;
    private MaterialButton Setting;
    private MaterialButton Download;
    //Card Container View//
    private CardView DeviceInfoCard;
    private CardView RecordHistoryCard;
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
    //Setting Group//
    private LinearLayout SettingLayout;
    private LinearLayout SettingInputLayoutText;
    private LinearLayout SettingInputLayoutText2;
    private LinearLayout SettingInputLayoutInterval;
    private LinearLayout SettingInputLayoutMeasurement;
    private TextView SiteName;
    private TextView IPAddress;
    private TextView Port;
    private TextView ZeroValue;
    private TextView Offset;
    private TextView RecordInterval;
    private TextView MeasurementMode;
    private AutoCompleteTextView SettingOpt;
    private TextView IntervalOpt;
    private EditText SettingInput;
    private EditText SettingInput2;
    private TextView SettingInputTitle;
    private TextView SettingInputTitle2;
    private EditText RecordIntervalInput;
    private Spinner IntervalOptInput;
    private Spinner MeasurementModeOpt;
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
        void saveDeviceList();
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
        MainLayout = v.findViewById(R.id.MainLayout);
        MainLayout.setLayoutTransition(new LayoutTransition());
        //Title Expandable View Button//
        DeviceInfo = v.findViewById(R.id.DeviceInfoTitle);
        RecordHistory = v.findViewById(R.id.RecordHistoryTitle);
        Setting = v.findViewById(R.id.SettingParameterTitle);
        Download = v.findViewById(R.id.DataLogDownloadTitle);
        //Card Container View//
        DeviceInfoCard = v.findViewById(R.id.DeviceInfoCard);
        RecordHistoryCard = v.findViewById(R.id.RecordHistoryCard);
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
        SettingInputLayoutMeasurement = v.findViewById(R.id.SettingMeasurementType);
        SiteName = v.findViewById(R.id.SiteName);
        IPAddress = v.findViewById(R.id.IPAddress);
        Port = v.findViewById(R.id.Port);
        ZeroValue = v.findViewById(R.id.ZeroValue);
        Offset = v.findViewById(R.id.SensorOffset);
        RecordInterval = v.findViewById(R.id.Interval);
        MeasurementMode = v.findViewById(R.id.MeasurementType);
        MeasurementModeOpt = v.findViewById(R.id.spinner5);
        SettingOpt = v.findViewById(R.id.spinner2content);

        String[] Options = new String[]{
                "Site Name","IP Address and Port","Sensor Offset and Zero Values", "Record Interval", "Measurement Mode"};

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
                TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                DeviceInfoLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(DeviceInfoCard, new AutoTransition());
                DeviceInfoLayout.setVisibility(View.VISIBLE);
            }
        });
        RecordHistory.setOnClickListener(view -> {
            if(RecordHistoryLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                RecordHistoryLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(RecordHistoryCard, new AutoTransition());
                RecordHistoryLayout.setVisibility(View.VISIBLE);
            }
        });
        Setting.setOnClickListener(view -> {
            if(SettingLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                SettingLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(SettingCard, new AutoTransition());
                SettingLayout.setVisibility(View.VISIBLE);
                if(!DeviceViewModel.getRealTimeStatus().getValue()&&!DeviceViewModel.getDownloadStatus().getValue()) {
                    if (DeviceViewModel.getConnectStatus().getValue() == 1)
                        listener.BTSend("LWST,7000000#\r\n");
                    else if (DeviceViewModel.getConnectStatus().getValue() == 2)
                        listener.BLESend("LWST,7000000#\r\n");
                }
            }
        });
        Download.setOnClickListener(view -> {
            if(DownloadLayout.getVisibility()==View.VISIBLE){
                TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                DownloadLayout.setVisibility(View.GONE);
            }
            else{
                TransitionManager.beginDelayedTransition(DownloadCard, new AutoTransition());
                DownloadLayout.setVisibility(View.VISIBLE);
            }
        });
        //Setting Input Handler//
//        SettingOpt.setAdapter(SettingAdapter);


        SettingOpt.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i){
                case 0:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("Site Name");
                    SettingInput.getText().clear();
                    SettingInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    SettingInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
                    SettingInputLayoutMeasurement.setVisibility(View.GONE);
//                    SetSetting.setEnabled(true);
                    break;
                case 1:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.VISIBLE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("IP Address");
                    SettingInputTitle2.setText("Port");
                    SettingInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    SettingInput2.setInputType(InputType.TYPE_CLASS_NUMBER);
                    SettingInput.getText().clear();
                    SettingInput.setFilters(new InputFilter[]{new InputFilterIP()});
                    SettingInput2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
                    SettingInputLayoutMeasurement.setVisibility(View.GONE);
//                    SetSetting.setEnabled(true);
                    break;
                case 2:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.VISIBLE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputTitle.setText("Offset (mm)");
                    SettingInput.getText().clear();
                    SettingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                    SettingInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
                    SettingInputLayoutMeasurement.setVisibility(View.GONE);
//                    SetSetting.setEnabled(true);
                    break;
                case 3:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.GONE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.VISIBLE);
                    SettingInputLayoutMeasurement.setVisibility(View.GONE);
//                    SetSetting.setEnabled(true);
                    break;
                case 4:
                    TransitionManager.beginDelayedTransition(SettingLayout, new AutoTransition());
                    SettingInputLayoutText.setVisibility(View.GONE);
                    SettingInputLayoutText2.setVisibility(View.GONE);
                    SettingInputLayoutInterval.setVisibility(View.GONE);
                    SettingInputLayoutMeasurement.setVisibility(View.VISIBLE);
//                    SetSetting.setEnabled(true);
                    break;
            }
        });

        IntervalOptInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()){
                    case "Minutes":
                        RecordIntervalInput.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "1439")});
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
            switch (text) {
                case "Site Name":
                    if (SettingInput.getText().toString().trim().length() > 4 && SettingInput.getText().toString().trim().length() < 21) {
                        if (validName(SettingInput.getText().toString().trim())) {
                            if (DeviceViewModel.getConnectStatus().getValue() == 1)
                                listener.BTSend("LWST,7780000#" + SettingInput.getText().toString().trim()+"\r\n");
                            else if (DeviceViewModel.getConnectStatus().getValue() == 2)
                                listener.BLESend("LWST,7780000#" + SettingInput.getText().toString().trim()+"\r\n");
                            DeviceViewModel.setSettingStatus(true);
                        } else
                            Snackbar.make(requireContext(), requireView(), "Site name cannot contains illegal character", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(requireContext(), requireView(), "Site name must be 5-20 character", Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "IP Address and Port":
                    if(IPAddress.getText().toString().trim().equals("Not Available")||Port.getText().toString().trim().equals("Not Available")){
                        Snackbar.make(requireContext(), requireView(), "This device model doesn't support IP and Port configuration", Snackbar.LENGTH_LONG).show();
                    }
                    else if (SettingInput.getText().toString().trim().length() != 0 && SettingInput2.getText().toString().trim().length() != 0) {
                        if (validIP(SettingInput.getText().toString().trim()) && isNumber(SettingInput2.getText().toString().trim())) {
                            if (DeviceViewModel.getConnectStatus().getValue() == 1)
                                listener.BTSend("LWST,SIP0000#" + SettingInput.getText().toString().trim() + "#" + SettingInput2.getText().toString().trim()+"\r\n");
                            else if (DeviceViewModel.getConnectStatus().getValue() == 2)
                                listener.BLESend("LWST,SIP0000#" + SettingInput.getText().toString().trim() + "#" + SettingInput2.getText().toString().trim()+"\r\n");
                            DeviceViewModel.setSettingStatus(true);
                        } else {
                            Snackbar.make(requireContext(), requireView(), "Please input valid IP and Port", Snackbar.LENGTH_SHORT).show();
                        }
                    } else if (IPAddress.getText().toString().trim().length() < 1) {
                        Snackbar.make(requireContext(), requireView(), "Please fill the IP address field", Snackbar.LENGTH_SHORT).show();
                    } else if (Port.getText().toString().trim().length() < 1) {
                        Snackbar.make(requireContext(), requireView(), "Please fill the port field", Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "Sensor Offset and Zero Values":
                    if (SettingInput.getText().toString().trim().length() != 0) {
                        if (isNumber(SettingInput.getText().toString().trim())) {
                            if (DeviceViewModel.getConnectStatus().getValue() == 1)
                                listener.BTSend(("LWST,7100000#" + SettingInput.getText().toString().trim())+"\r\n");
                            else if (DeviceViewModel.getConnectStatus().getValue() == 2)
                                listener.BLESend("LWST,7100000#" + SettingInput.getText().toString().trim()+"\r\n");
                            DeviceViewModel.setSettingStatus(true);
                        } else
                            Snackbar.make(requireContext(), requireView(), "Offset must be a number", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(requireContext(), requireView(), "Please fill the offset value field", Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "Record Interval":
                    int interval;
                    if (RecordIntervalInput.getText().toString().trim().length() != 0) {
                        if (isNumber(RecordIntervalInput.getText().toString().trim())) {
                            if (IntervalOptInput.getSelectedItemPosition() == 0)
                                interval = 60 * Integer.parseInt(String.valueOf(RecordIntervalInput.getText()));
                            else
                                interval = 3600 * Integer.parseInt(String.valueOf(RecordIntervalInput.getText()));

                            if (DeviceViewModel.getConnectStatus().getValue() == 1)
                                listener.BTSend("LWST,7220000#" + interval +"\r\n");
                            else if (DeviceViewModel.getConnectStatus().getValue() == 2)
                                listener.BLESend("LWST,7220000#" + interval +"\r\n");
                            DeviceViewModel.setSettingStatus(true);
                        } else
                            Snackbar.make(requireContext(), requireView(), "Interval must be a number", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(requireContext(), requireView(), "Please fill the record interval field", Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "Measurement Mode":
                    if(DeviceViewModel.getDeviceModel().getValue().contains("Promithevo-U"))
                        Snackbar.make(requireContext(), requireView(), "This device model doesn't support changing measurement mode", Snackbar.LENGTH_SHORT).show();
                    else{
                        if(DeviceViewModel.getConnectStatus().getValue() == 1){
                            if(MeasurementModeOpt.getSelectedItemPosition()==0)
                                listener.BTSend("LWST,6000000#1\r\n");
                            else
                                listener.BTSend("LWST,6000000#0\r\n");
                        }else if(DeviceViewModel.getConnectStatus().getValue() == 2){
                            if(MeasurementModeOpt.getSelectedItemPosition()==0)
                                listener.BLESend("LWST,6000000#1\r\n");
                            else
                                listener.BLESend("LWST,6000000#0\r\n");
                        }
                    }
                    break;
            }
        });
        TimeSync.setOnClickListener(view -> {
            String TimeSynchronize = "0";
            if(DeviceViewModel.getConnectStatus().getValue()==1)
                TimeSynchronize = new SimpleDateFormat("yy/MM/dd,HH:mm:ss").format(new Date());
            else if(DeviceViewModel.getConnectStatus().getValue()==2)
                TimeSynchronize = new SimpleDateFormat("yy,MM,dd,HH,mm,ss").format(new Date());
            String TimeZone;
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
                    listener.BLESend("LWTS,"+ TimeSynchronize+"\r\n");
            }
            else{
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWTS\""+ TimeSynchronize+TimeZoneInt+"\"\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWTS,"+ TimeSynchronize+"\"\r\n");
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
                    (view12, year, monthOfYear, dayOfMonth) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date DateToCompare = null;
                        try {
                            DateToCompare = sdf.parse((dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date DateToCompare2 = null;
                        try {
                            DateToCompare2 = sdf.parse(FirstRecordedDate.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            DateToCompare2 = new Date();
                        }

                        Date DateToCompare3 = null;
                        try {
                            DateToCompare3 = sdf.parse(LastRecordedDate.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            DateToCompare3 = new Date();
                        }

                        assert DateToCompare != null;
                        assert DateToCompare3 != null;
                        if (DateToCompare.getTime() > DateToCompare3.getTime()) {
                            StartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare3));
                            String[] SplitString = StartDate.getText().toString().split("/");
                            sYear = Integer.parseInt(SplitString[2]);
                            sMonth = Integer.parseInt(SplitString[1]);
                            sDay = Integer.parseInt(SplitString[0]);
                        } else {
                            assert DateToCompare2 != null;
                            if (DateToCompare.getTime() < DateToCompare2.getTime()) {
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
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date DateToCompare = null;

                        try {
                            DateToCompare = sdf.parse((dayOfMonth + "/" + (monthOfYear + 1) + "/" + year));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Date DateToCompare2 = null;
                        try {
                            DateToCompare2 = sdf.parse(FirstRecordedDate.getText().toString().trim());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            DateToCompare2 = new Date();
                        }

                        Date DateToCompare3 = null;
                        try {
                            DateToCompare3 = sdf.parse(LastRecordedDate.getText().toString().trim());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            DateToCompare3 = new Date();
                        }

                        assert DateToCompare != null;
                        assert DateToCompare3 != null;
                        if (DateToCompare.getTime() > DateToCompare3.getTime()) {
                            EndDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(DateToCompare3));
                            String[] SplitString = StartDate.getText().toString().split("/");
                            sYear = Integer.parseInt(SplitString[2]);
                            sMonth = Integer.parseInt(SplitString[1]);
                            sDay = Integer.parseInt(SplitString[0]);
                        } else {
                            assert DateToCompare2 != null;
                            if (DateToCompare.getTime() < DateToCompare2.getTime()) {
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
            int DayofTheYear;
            int DownloadLength;
            if(StartDate.getText().toString().isEmpty())
                Snackbar.make(requireContext(), requireView(),"Please fill the start date",Snackbar.LENGTH_SHORT).show();
            else if(EndDate.getText().toString().isEmpty())
                Snackbar.make(requireContext(), requireView(),"Please fill the end date",Snackbar.LENGTH_SHORT).show();
            else if(FirstRecordedDate==null||LastRecordedDate==null)
                Snackbar.make(requireContext(), requireView(), "There is currently no data to be downloaded", Snackbar.LENGTH_SHORT).show();
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

                if(endDateValue==null||startDateValue==null){
                    Snackbar.make(requireContext(), requireView(), "Please enter a valid date format \"dd/mm/yyyy\"", Snackbar.LENGTH_SHORT).show();
                }else{

                    long diff = endDateValue.getTime() - startDateValue.getTime();
                    assert startFirstFayOfTheYear != null;
                    long diff2 = startDateValue.getTime() - startFirstFayOfTheYear.getTime();
                    DownloadLength = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
                    DayofTheYear = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;

                    if (DownloadLength <= 0) {
                        Snackbar.make(requireContext(), requireView(), "Start date must be older than end date", Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        if(DeviceViewModel.getConnectStatus().getValue()==1){
                            listener.BTStartDownload(DownloadLength,DayofTheYear,sYear);
                            listener.BTSend(String.format("LWDL,%02d,%03d,%03d,\r\n",(sYear - 2000),DayofTheYear,DownloadLength));
                        }else if(DeviceViewModel.getConnectStatus().getValue()==2){
                            listener.BLEStartDownload(DownloadLength,DayofTheYear,sYear);
                            listener.BLESend(String.format("LWDL,%02d,%03d,%03d,\r\n",(sYear - 2000),DayofTheYear,DownloadLength));
                        }
                        DeviceViewModel.setDownloadStatus(true);
                    }
                }
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
        DeviceViewModel.getIPAddress().observe(getViewLifecycleOwner(), s -> {
                if (s.equals("0"))
                    IPAddress.setText("Not Available");
                else
                    IPAddress.setText(s);
                 });
        DeviceViewModel.getPort().observe(getViewLifecycleOwner(), s -> {
                    if (s.equals("0"))
                        Port.setText("Not Available");
                    else
                        Port.setText(s);
                });
        DeviceViewModel.getSensorZeroValues().observe(getViewLifecycleOwner(), s -> ZeroValue.setText(s));
        DeviceViewModel.getSensorOffset().observe(getViewLifecycleOwner(), s -> Offset.setText(s));
        DeviceViewModel.getRecordInterval().observe(getViewLifecycleOwner(), s -> {
            int intervalRec;
            try {
                intervalRec = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                intervalRec=0;
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
        DeviceViewModel.getMeasurementMode().observe(getViewLifecycleOwner(), integer -> {
            if(integer==0){
                MeasurementMode.setText("Tide gauge");
            }
            else if(integer==1)
                MeasurementMode.setText("Ground water");
        });
        DeviceViewModel.getSettingStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(!aBoolean) {
                Snackbar.make(requireContext(), requireView(), "Setting Success", Snackbar.LENGTH_SHORT).show();
                DeviceViewModel.setSettingStatus(true);
                SetSetting.setEnabled(true);
            }else{
                SetSetting.setEnabled(false);
            }
        });
        DeviceViewModel.getSyncStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                Snackbar.make(requireContext(), requireView(), "Time Synchronization Success", Snackbar.LENGTH_SHORT).show();
                DeviceViewModel.setSyncStatus(false);
            }
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
                SetSetting.setEnabled(false);
            }else{
                StartDownload.setEnabled(true);
                SetSetting.setEnabled(true);
            }
        });
        DeviceViewModel.getConnectStatus().observe(getViewLifecycleOwner(), integer -> {
            if(integer==0){
                requireActivity().onBackPressed();
            }
        });
        DeviceViewModel.getRealTimeStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                SetSetting.setEnabled(false);
                StartDownload.setEnabled(false);
            }
            else {
                SetSetting.setEnabled(true);
                StartDownload.setEnabled(true);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        String[] Options = new String[]{
                "Site Name", "IP Address and Port", "Sensor Offset and Zero Values", "Record Interval", "Measurement Mode"};
        ArrayAdapter<String> SettingAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.setting_option, Options
        );
        SettingOpt.getText().clear();
        SettingOpt.setAdapter(SettingAdapter);
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

    private boolean validIP (String ip) {

        String[] parts = ip.split( "\\." );

        try {
            if (ip.isEmpty()) {
                return false;
            }
            else if ( parts.length != 4 ) {
                return false;
            }
            else return isNumber(parts[0]) && isNumber(parts[1]) && isNumber(parts[2]) && isNumber(parts[3]);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private boolean validName (String name) {
        String illegalChar = "!@#$%&*()’+,/:;<=>?[]^`{|}";
        if(EmojiManager.containsEmoji(name))
            return false;
        for (int i = 0; i < name.length(); i++) {
            String strChar = Character.toString(name.charAt(i));
            // Check whether String contains special character or not
            if (illegalChar.contains(strChar)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String s){
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}