package com.example.luwesmobileapps.ui.dataviewer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.filter.InputFilterIP;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DataViewerFragment extends Fragment {

    private SharedViewModel DeviceViewModel;
    private TabLayout Tabs;
    private TabLayout Tabs2;
    private fragmentListener listener;
    private RelativeLayout CachedDataGroup;
    private MaterialCardView DetailsGroup;

    //Live Data Group
    private RelativeLayout LiveDataGroup;
    private LineChart LiveCharts;
    private TextView DeviceDateTime;
    private TextView DeviceBattery;
    private TextView RecordStatus;
    private TextView InternetConnectionStatus;
    private MaterialButton Listen;
    private String prevTimeStamp;
    private long FirstTimestamp;
    //Cached Data Group
    private LineChart CachedCharts;
    private CircularProgressIndicator LoadProgress;
    private FileAccess myFileAccess = new FileAccess();
    private AutoCompleteTextView DeviceListOpt;
    private TextInputLayout StartDate;
    private TextInputLayout EndDate;
    private MaterialButton Plot;
    private MaterialButton Generate;
    private ArrayList<FileAccess.plottingData> data = new ArrayList<>();
    //Export Data Group
    private TextInputLayout SelectMode;
    private AutoCompleteTextView ExportMode;
    private LinearLayout TargetTCP;
    private TextInputLayout TCPIP;
    private TextInputLayout TCPPort;

    public interface fragmentListener{
        void BTSend(String string);
        void BLESend(String string);
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DeviceViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dataviewer, container, false);
        RelativeLayout MainLayout = root.findViewById(R.id.MainLayoutData);
        Tabs = root.findViewById(R.id.Tab);
        Tabs2 = root.findViewById(R.id.Tab2);
        DetailsGroup = root.findViewById(R.id.DetailsContainer);
        //Export Data Group
        TargetTCP = root.findViewById(R.id.targetTCP);
        SelectMode = root.findViewById(R.id.selectMode);
        ExportMode = root.findViewById(R.id.selectModeContent);
        TCPIP = root.findViewById(R.id.ipAddress);
        TCPPort = root.findViewById(R.id.port);
        Generate = root.findViewById(R.id.Generate);
        //Cached Data Group
        CachedDataGroup = root.findViewById(R.id.CachedDataGroup);
        LoadProgress = root.findViewById(R.id.loadProgress);
        DeviceListOpt = root.findViewById(R.id.selectDeviceContent);
        StartDate = root.findViewById(R.id.startDate);
        EndDate = root.findViewById(R.id.endDate);
        Plot = root.findViewById(R.id.Plot);
        //Live Data Group
        LiveDataGroup = root.findViewById(R.id.LiveDataGroup);
        DeviceDateTime = root.findViewById(R.id.DateTime);
        DeviceBattery = root.findViewById(R.id.Battery);
        RecordStatus = root.findViewById(R.id.Record);
        InternetConnectionStatus = root.findViewById(R.id.InternetConnection);
        Listen = root.findViewById(R.id.realtimebutton);

        BadgeDrawable LiveBadge = Objects.requireNonNull(Tabs.getTabAt(1)).getOrCreateBadge();
        BadgeDrawable CachedBadge = Objects.requireNonNull(Tabs.getTabAt(0)).getOrCreateBadge();
        LiveBadge.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorSecondary));
        LiveBadge.setVisible(false);
        CachedBadge.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        CachedBadge.setVisible(false);

        TCPIP.getEditText().setFilters(new InputFilter[]{new InputFilterIP()});
        TCPPort.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        TCPPort.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        Listen.setEnabled(false);
        Tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int checkedId = Tabs.getSelectedTabPosition();
                switch (checkedId) {
                    case 0:
                        TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                        CachedDataGroup.setVisibility(View.VISIBLE);
                        LiveDataGroup.setVisibility(View.GONE);
                        CachedCharts.setVisibility(View.VISIBLE);
                        LiveCharts.setVisibility(View.INVISIBLE);
                        SelectMode.setVisibility(View.GONE);
                        Tabs2.setVisibility(View.VISIBLE);
                        Generate.setVisibility(View.GONE);
                        Plot.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                        CachedDataGroup.setVisibility(View.GONE);
                        LiveDataGroup.setVisibility(View.VISIBLE);
                        LiveCharts.setVisibility(View.VISIBLE);
                        CachedCharts.setVisibility(View.INVISIBLE);
                        SelectMode.setVisibility(View.GONE);
                        Tabs2.setVisibility(View.GONE);
                        break;
                    case 2:
                        TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                        CachedDataGroup.setVisibility(View.VISIBLE);
                        LiveDataGroup.setVisibility(View.GONE);
                        CachedCharts.setVisibility(View.GONE);
                        LiveCharts.setVisibility(View.INVISIBLE);
                        SelectMode.setVisibility(View.VISIBLE);
                        Tabs2.setVisibility(View.GONE);
                        Generate.setVisibility(View.VISIBLE);
                        Plot.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        SelectMode.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(SelectMode.getEditText().getText().toString().contains("Online")){
                    TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                    TargetTCP.setVisibility(View.VISIBLE);
                }else{
                    TransitionManager.beginDelayedTransition(MainLayout, new AutoTransition());
                    TargetTCP.setVisibility(View.GONE);
                }

            }
        });

        Tabs2.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(CachedCharts.getData()!=null){
                    CachedCharts.getData().clearValues();
                    postData(data,CachedCharts);
                    CachedCharts.animateX(2000);
                    CachedCharts.invalidate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
//
//        ArrayAdapter<String> DeviceListAdapter = new ArrayAdapter<>(
//                getActivity(),
//                R.layout.device_option,myFileAccess.LoadDeviceList()
//        );

        Objects.requireNonNull(StartDate.getEditText()).setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    StartDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                }
            }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        Objects.requireNonNull(EndDate.getEditText()).setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    EndDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                }
            }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

        });

        Plot.setOnClickListener(view -> {
            if(Plot.getText().toString().contains("Plot")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date startDate = null;
                try {
                    startDate = sdf.parse(StartDate.getEditText().getText().toString() + " 00:00:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date endDate = null;
                try {
                    endDate = sdf.parse(EndDate.getEditText().getText().toString() + " 23:59:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if ((startDate != null) && (endDate != null) && (!DeviceListOpt.getText().toString().isEmpty())) {
                    data.clear();
                    CachedCharts.getData().clearValues();
                    Thread LoadThread = new LoadData(DeviceListOpt.getText().toString(), data, startDate, endDate, true);
                    LoadThread.start();
                    Plot.setEnabled(false);
                    LoadProgress.setVisibility(View.VISIBLE);
                    CachedBadge.setVisible(true);
                } else {
                    Snackbar.make(getContext(), requireView(), "Please input required parameter first", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        Generate.setOnClickListener(view -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date startDate = null;
            try {
                startDate = sdf.parse(StartDate.getEditText().getText().toString()+" 00:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date endDate = null;
            try {
                endDate = sdf.parse(EndDate.getEditText().getText().toString()+" 23:59:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if((startDate!=null)&&(endDate!=null)&&(!DeviceListOpt.getText().toString().isEmpty())){
                data.clear();
                CachedCharts.getData().clearValues();
                if(SelectMode.getEditText().getText().toString().contains("Online")){
                    Thread UploadThread = new UploadData(DeviceListOpt.getText().toString(),TCPIP.getEditText().getText().toString()
                            ,Integer.parseInt(TCPPort.getEditText().getText().toString()),startDate,endDate);
                    UploadThread.start();
                }else{
                    Thread LoadThread = new LoadData(DeviceListOpt.getText().toString(),data,startDate,endDate,false);
                    LoadThread.start();
                }
                Generate.setEnabled(false);
                LoadProgress.setVisibility(View.VISIBLE);
                CachedBadge.setVisible(true);
            }else{
                Snackbar.make(getContext(), requireView(),"Please input required parameter first",Snackbar.LENGTH_SHORT).show();
            }
        });

        Listen.setOnClickListener(view -> {
            if((Listen.getText().toString()).equalsIgnoreCase("Listen")){
                LiveCharts.clear();
                setupChartRT(LiveCharts,DeviceViewModel.getNightMode().getValue());
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWRT,\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWRT,\r\n");
                DeviceViewModel.setRealTimeStatus(true);
            }
            else if((Listen.getText().toString()).equalsIgnoreCase("Stop")){
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWST,7000000#\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWST,7000000#\r\n");
            }
        });

        CachedCharts = root.findViewById(R.id.lineChart);
        LiveCharts = root.findViewById(R.id.lineChart2);
        setupChart(CachedCharts,DeviceViewModel.getNightMode().getValue());
        setupChartRT(LiveCharts,DeviceViewModel.getNightMode().getValue());

        //View Model Callbacks Response//
        DeviceViewModel.getDeviceDateTime().observe(getViewLifecycleOwner(), s -> DeviceDateTime.setText(s));
        DeviceViewModel.getDeviceBattery().observe(getViewLifecycleOwner(), s -> {
            if(!s.equals("")){
                DeviceBattery.setText(s+" v");
            }else{
                DeviceBattery.setText(s);
            }
        });
        DeviceViewModel.getRecordStatus().observe(getViewLifecycleOwner(), s -> RecordStatus.setText(s));
        DeviceViewModel.getWaterLevel().observe(getViewLifecycleOwner(), s -> {
            if(!s.equals("")&&!DeviceDateTime.getText().toString().equals(prevTimeStamp)) {
                addEntry(LiveCharts, Float.parseFloat(s));
                prevTimeStamp=DeviceDateTime.getText().toString();
            }
        });
        DeviceViewModel.getInternetConnectionStatus().observe(getViewLifecycleOwner(), s -> InternetConnectionStatus.setText(s));
        DeviceViewModel.getRealTimeStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                Listen.setText("Stop");
                LiveBadge.setVisible(true);
            }
            else{
                Listen.setText("Listen");
                LiveBadge.setVisible(false);
            }
        });
        DeviceViewModel.getConnectStatus().observe(getViewLifecycleOwner(), integer -> {
            if(integer==0){
                Listen.setEnabled(false);
            }else{
                Listen.setEnabled(true);
            }
        });
        DeviceViewModel.getGenerateData().observe(getViewLifecycleOwner(), (Observer<Integer>) stat -> {
            if(stat!=0){
                if (!data.isEmpty()) {
                    if(stat==1){
                        postData(data, DataViewerFragment.this.CachedCharts);
                        CachedCharts.animateX(2000);
                        CachedCharts.invalidate();
                    }
                    else if(stat==2) {
                        Snackbar.make(requireContext(), DataViewerFragment.this.requireView(), "Batched data file successfully generated and saved in " + DeviceListOpt.getText().toString() + " records folder", Snackbar.LENGTH_LONG).show();
                    }
                    else if(stat==3) {
                        Snackbar.make(requireContext(), DataViewerFragment.this.requireView(), "Records successfully uploaded to the server", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(requireContext(), DataViewerFragment.this.requireView(), "No records for selected time span", Snackbar.LENGTH_SHORT).show();
                }
                LoadProgress.setVisibility(View.INVISIBLE);
                CachedBadge.setVisible(false);
                if(stat==1) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            DataViewerFragment.this.Plot.setEnabled(true); }
                        }, 3000);
                }
                else if(stat==2||stat==3)
                    DataViewerFragment.this.Generate.setEnabled(true);
                DeviceViewModel.setGenerateData(0);
            }
        });
        return root;
    }

    private void setupChart(LineChart chart, boolean NightMode) {
        LineData data = new LineData();
        chart.setData(data);
        chart.calculateOffsets();
        if(NightMode)
            chart.setBackgroundColor(requireContext().getResources().getColor(R.color.colorDarkGray));
        else
            chart.setBackgroundColor(Color.WHITE);

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setPinchZoom(false);
        CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.graph_marker);
        mv.setChartView(chart);
        chart.setMarker(mv);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawGridLines(true);
        x.setDrawAxisLine(true);
        if(NightMode)
            x.setGridColor(Color.WHITE);
        else
            x.setTextColor(Color.DKGRAY);

        if(NightMode)
            x.setTextColor(Color.WHITE);
        else
            x.setTextColor(Color.DKGRAY);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(3, true);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);
        x.setValueFormatter(new XValueFormatter());

        YAxis y = chart.getAxisLeft();
        y.setDrawLabels(true);
        y.setLabelCount(10, true);
        if(NightMode)
            y.setGridColor(Color.WHITE);
        else
            y.setTextColor(Color.DKGRAY);

        if(NightMode)
            y.setTextColor(Color.WHITE);
        else
            y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(true);
        y.setDrawAxisLine(true);
        y.setValueFormatter(new YValueFormatter());

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        if(NightMode)
            legend.setTextColor(Color.WHITE);
        else
            legend.setTextColor(Color.DKGRAY);
        legend.setEnabled(true);
        legend.setDrawInside(false);
        // don't forget to refresh the drawing
        chart.invalidate();
    }

    class XValueFormatter extends ValueFormatter {
        XValueFormatter() {
        }

        public String getAxisLabel(float value, AxisBase axis) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date startDate = null;
            try {
                startDate = sdf.parse(Objects.requireNonNull(DataViewerFragment.this.StartDate.getEditText()).getText().toString() + " 00:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date endDate = null;
            try {
                endDate = sdf.parse(Objects.requireNonNull(DataViewerFragment.this.EndDate.getEditText()).getText().toString() + " 23:59:00");
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            assert endDate != null;
            assert startDate != null;
            int timeSpan = ((int) TimeUnit.DAYS.convert(endDate.getTime() - startDate.getTime(), TimeUnit.MILLISECONDS)) + 1;
//            long newValue = ((FileAccess.plottingData) DataViewerFragment.this.data.get((int) value)).getTimestamp();
            long newValue = ((long) value)*60000+FirstTimestamp;
            if (timeSpan <= 1) {
                return new SimpleDateFormat("HH:mm").format(new Date(newValue));
            }
            if (timeSpan > 28) {
                return new SimpleDateFormat("dd/MM/yyyy HH:00").format(new Date(newValue));
            }
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(newValue));
        }
    }

    static class XValueFormatterRT extends ValueFormatter{
        @Override
        public String getAxisLabel(float value,AxisBase axis) {
            long convertedValue= TimeUnit.SECONDS.toMillis((long) value);
            String timestamp;
            timestamp = new SimpleDateFormat("mm:ss").format(new Date(convertedValue));
            return timestamp;
        }
    }

    class YValueFormatter extends ValueFormatter{
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            if(Tabs2.getSelectedTabPosition()==0)
                return String.format("%.2f",value)+" m";
            else
                return String.format("%.2f",value)+"%";
        }
    }

    static class YValueFormatterRT extends ValueFormatter{
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format("%.2f m",value);
        }
    }

    public void postData(ArrayList<FileAccess.plottingData> data, LineChart chart) {
        ArrayList<Entry> values = new ArrayList<>();
        int x = 0;
        for(FileAccess.plottingData bData:data){
            Entry newEntry;
            if(x==0){
                FirstTimestamp = bData.getTimestamp();
            }
            long newX = (bData.getTimestamp()-FirstTimestamp)/60000;
            if(Tabs2.getSelectedTabPosition()==1)
                 newEntry = new Entry((float) newX, bData.getBatteryPercentage());
            else
                 newEntry = new Entry((float) newX, bData.getWaterLevel());
            values.add(newEntry);
            x++;
            Log.d("Plotter", "postData: " + values.get(values.indexOf(newEntry)).getX());
        }
        if (chart.getData() == null || ((LineData) chart.getData()).getDataSetCount() <= 0) {
            LineDataSet set1 = new LineDataSet(values, this.DeviceListOpt.getText().toString());
//            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(false);
            set1.setDrawCircles(true);
            set1.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
            set1.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
            set1.setCircleRadius(3.0f);
            set1.setLineWidth(1.8f);
            set1.setHighLightColor(ContextCompat.getColor(getContext(), R.color.colorSecondary));
            set1.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            set1.setDrawHorizontalHighlightIndicator(true);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
            LineData dataL = new LineData(set1);
            dataL.setValueTextSize(9.0f);
            dataL.setDrawValues(false);
            chart.setData(dataL);
            return;
        }
        ((LineDataSet) ((LineData) chart.getData()).getDataSetByIndex(0)).setValues(values);
        ((LineData) chart.getData()).notifyDataChanged();
        chart.notifyDataSetChanged();
    }

    private void setupChartRT(LineChart chart, boolean NightMode){
        LineData data = new LineData();
        chart.setData(data);
        chart.calculateOffsets();
        if(NightMode)
            chart.setBackgroundColor(requireContext().getResources().getColor(R.color.colorDarkGray));
        else
            chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setPinchZoom(false);
//        CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.graph_marker);
//        mv.setChartView(chart);
//        chart.setMarker(mv);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawGridLines(true);
        x.setDrawAxisLine(true);
        if(NightMode)
            x.setGridColor(Color.WHITE);
        else
            x.setTextColor(Color.DKGRAY);

        if(NightMode)
            x.setTextColor(Color.WHITE);
        else
            x.setTextColor(Color.DKGRAY);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(3, true);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);
        x.setValueFormatter(new XValueFormatterRT());

        YAxis y = chart.getAxisLeft();
        y.setDrawLabels(true);
        y.setLabelCount(10, true);
        if(NightMode)
            y.setGridColor(Color.WHITE);
        else
            y.setTextColor(Color.DKGRAY);

        if(NightMode)
            y.setTextColor(Color.WHITE);
        else
            y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(true);
        y.setDrawAxisLine(true);
        y.setValueFormatter(new YValueFormatterRT());

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        if(NightMode)
            legend.setTextColor(Color.WHITE);
        else
            legend.setTextColor(Color.DKGRAY);
        legend.setEnabled(true);
        legend.setDrawInside(false);
    }

    private void addEntry(LineChart chart,Float waterLevel) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(data.getEntryCount(), waterLevel),0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, DeviceViewModel.getSiteName().getValue());
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ContextCompat.getColor(requireContext(),R.color.colorPrimary));
//        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        set.setCubicIntensity(0.2f);
        set.setDrawFilled(false);
        set.setDrawCircles(false);
        set.setLineWidth(1.8f);
        set.setHighLightColor(ContextCompat.getColor(requireContext(),R.color.colorSecondary));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    class LoadData extends Thread{
        private String deviceName;
        private Date startDate;
        private Date endDate;
        private Boolean plottable;
        private ArrayList<FileAccess.plottingData> plotData;

        LoadData(String deviceNameInput, ArrayList<FileAccess.plottingData> plotDataInput,
                 Date startDateInput, Date endDateInput, Boolean plot){
            this.deviceName = deviceNameInput;
            this.startDate = startDateInput;
            this.endDate = endDateInput;
            this.plotData = plotDataInput;
            this.plottable = plot;
        }

        @Override
        public void run() {
            myFileAccess.LoadPlotData(deviceName,plotData,startDate,endDate,plottable);
            if(plottable)
                DeviceViewModel.postGenerateData(1);
            else
                DeviceViewModel.postGenerateData(2);
        }
    }

    class UploadData extends Thread{
        private String deviceName;
        private Date startDate;
        private Date endDate;
        private String IP;
        private Integer Port;


        UploadData(String deviceNameInput, String IP, Integer Port,
                 Date startDateInput, Date endDateInput){
            this.deviceName = deviceNameInput;
            this.startDate = startDateInput;
            this.endDate = endDateInput;
            this.IP = IP;
            this.Port = Port;
        }

        @Override
        public void run() {
            myFileAccess.UploadFile(deviceName,IP,Port,startDate,endDate,data);
            DeviceViewModel.postGenerateData(3);
        }
    }

    public class CustomMarkerView extends MarkerView {
        private TextView tValueX1 = ((TextView) findViewById(R.id.tvContentX1));
        private TextView tValueX2 = ((TextView) findViewById(R.id.tvContentX2));
        private TextView tValueY = ((TextView) findViewById(R.id.tvContentY));

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
        }

        public void refreshContent(Entry e, Highlight highlight) {
            if(Tabs2.getSelectedTabPosition()==0)
                tValueY.setText(String.format("%.2f",e.getY()) +" m");
            else
                tValueY.setText(String.format("%.2f",e.getY()) +"%");
//            long timestamp = ((FileAccess.plottingData) DataViewerFragment.this.data.get((int) e.getX())).getTimestamp();
            long timestamp = ((long) e.getX())*60000+FirstTimestamp;
            tValueX1.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date(timestamp)));
            tValueX2.setText(new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp)));
            super.refreshContent(e, highlight);
        }

        public MPPointF getOffset() {
            return new MPPointF((float) (-(getWidth() + 5)), (float) (-(getHeight() + 5)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String[] modeList = {"Local(Phone storage directory)","Online(TCP Server)"};
        ArrayAdapter<String> DeviceListAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.device_option,myFileAccess.LoadDeviceList()
        );
        ArrayAdapter<String> ExportModeAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.device_option,modeList
        );
        DeviceListOpt.getText().clear();
        DeviceListOpt.setAdapter(DeviceListAdapter);
        ExportMode.getText().clear();
        ExportMode.setAdapter(ExportModeAdapter);
    }
}