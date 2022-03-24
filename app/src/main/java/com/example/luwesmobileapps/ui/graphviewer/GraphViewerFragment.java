package com.example.luwesmobileapps.ui.graphviewer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.ui.devicepage.DevicePageFragment;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GraphViewerFragment extends Fragment {

    private SharedViewModel DeviceViewModel;
    private MaterialButtonToggleGroup ButtonGroup;
    private fragmentListener listener;
    private RelativeLayout CachedDataGroup;
    private RelativeLayout DetailsGroup;

    //Live Data Group
    private RelativeLayout LiveDataGroup;
    private TextView DeviceDateTime;
    private TextView DeviceBattery;
    private TextView RecordStatus;
    private TextView InternetConnectionStatus;
    private MaterialButton Listen;

    private LineChart charts;
    private FileAccess myFileAccess = new FileAccess();
    private AutoCompleteTextView DeviceListOpt;
    private TextInputLayout StartDate;
    private TextInputLayout EndDate;
    private MaterialButton Generate;
    private ArrayList<FileAccess.plottingData> data = new ArrayList<>();

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
        View root = inflater.inflate(R.layout.fragment_graphviewer, container, false);
        ButtonGroup = root.findViewById(R.id.ButtonGroup);
        //Cached Data Group
        CachedDataGroup = root.findViewById(R.id.CachedDataGroup);
        Button CachedDataButton = root.findViewById(R.id.CachedData);
        DetailsGroup = root.findViewById(R.id.DetailsContainer);
        DeviceListOpt = root.findViewById(R.id.selectDeviceContent);
        StartDate = root.findViewById(R.id.startDate);
        EndDate = root.findViewById(R.id.endDate);
        Generate = root.findViewById(R.id.Generate);
        //Live Data Group
        LiveDataGroup = root.findViewById(R.id.LiveDataGroup);
        Button LiveDataButton = root.findViewById(R.id.LiveData);
        DeviceDateTime = root.findViewById(R.id.DateTime);
        DeviceBattery = root.findViewById(R.id.Battery);
        RecordStatus = root.findViewById(R.id.Record);
        InternetConnectionStatus = root.findViewById(R.id.InternetConnection);
        Listen = root.findViewById(R.id.realtimebutton);

        Listen.setEnabled(false);

        ButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            switch (checkedId){
                case R.id.CachedData:
                    TransitionManager.beginDelayedTransition(DetailsGroup,new AutoTransition());
                    CachedDataGroup.setVisibility(View.VISIBLE);
                    LiveDataGroup.setVisibility(View.GONE);
                    charts.clear();
                    break;
                case R.id.LiveData:
                    TransitionManager.beginDelayedTransition(DetailsGroup, new AutoTransition());
                    CachedDataGroup.setVisibility(View.GONE);
                    LiveDataGroup.setVisibility(View.VISIBLE);
                    charts.clear();
                    break;
            }
        });

        ArrayAdapter<String> DeviceListAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.device_option,myFileAccess.LoadDeviceList()
        );

        StartDate.getEditText().setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    StartDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                }
            }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        EndDate.getEditText().setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    EndDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                }
            }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

        });

        Generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    myFileAccess.LoadPlotData(DeviceListOpt.getText().toString(),data,startDate,endDate);
                    if(!data.isEmpty())
                        setupChart(data,charts);
                    else
                        Toast.makeText(getContext(),"No records for selected time span",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"Please input required parameter first",Toast.LENGTH_SHORT).show();
                }
            }
        });

        Listen.setOnClickListener(view -> {
            if((Listen.getText().toString()).equalsIgnoreCase("Listen")){
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWRT,\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWRT,\r\n");
                DeviceViewModel.setRealTimeStatus(true);
                setupChartRT(charts);
            }
            else if((Listen.getText().toString()).equalsIgnoreCase("Stop")){
                if(DeviceViewModel.getConnectStatus().getValue()==1)
                    listener.BTSend("LWST,7000000#\r\n");
                else if(DeviceViewModel.getConnectStatus().getValue()==2)
                    listener.BLESend("LWST,7000000#\r\n");
            }
        });

        DeviceListOpt.setAdapter(DeviceListAdapter);
        charts = root.findViewById(R.id.lineChart);

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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if(!s.equals(""))
                addEntry(charts, Float.parseFloat(s));
        });
        DeviceViewModel.getInternetConnectionStatus().observe(getViewLifecycleOwner(), s -> InternetConnectionStatus.setText(s));
        DeviceViewModel.getRealTimeStatus().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                Listen.setText("Stop");
                CachedDataButton.setEnabled(false);
            }
            else{
                Listen.setText("Listen");
                CachedDataButton.setEnabled(true);
                charts.clear();
            }
        });
        DeviceViewModel.getConnectStatus().observe(getViewLifecycleOwner(), integer -> {
            if(integer==0){
                Listen.setEnabled(false);
            }else{
                Listen.setEnabled(true);
            }
        });
        return root;
    }

    private void setupChart(ArrayList<FileAccess.plottingData> data, LineChart chart) {

        postData(data,chart);
        chart.calculateOffsets();
        chart.setBackgroundColor(Color.WHITE);
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawGridLines(true);
        x.setDrawAxisLine(true);
        x.setTextColor(Color.DKGRAY);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(4, true);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);
        x.setValueFormatter(new XValueFormatter());

        YAxis y = chart.getAxisLeft();
        y.setDrawLabels(true);
        y.setLabelCount(10, true);
        y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(true);
        y.setDrawAxisLine(true);
        y.setValueFormatter(new YValueFormatter());

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setDrawInside(false);

        chart.animateX(2000);
        // don't forget to refresh the drawing
        chart.invalidate();
    }

    class XValueFormatter extends ValueFormatter{
        @Override
        public String getAxisLabel(float value,AxisBase axis) {
            long convertedValue= TimeUnit.MINUTES.convert((long) value,TimeUnit.MILLISECONDS);
            String timestamp;
                timestamp = new SimpleDateFormat("HH:mm").format(new Date((long)value));
            return timestamp;
        }
    }

    class YValueFormatter extends ValueFormatter{
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format("%.2f m",value);
        }
    }

    private void postData(ArrayList<FileAccess.plottingData> data,LineChart chart) {

        ArrayList<Entry> values = new ArrayList<>();

        for (FileAccess.plottingData bData : data) {
            values.add(new Entry(bData.getTimestamp(), bData.getWaterLevel()));
        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, DeviceListOpt.getText().toString());

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(false);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setHighLightColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryVariant));
            set1.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
            set1.setDrawHorizontalHighlightIndicator(true);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

            // create a data object with the data sets
            LineData dataL = new LineData(set1);
            dataL.setValueTextSize(9f);
            dataL.setDrawValues(true);

            // set data
            chart.setData(dataL);
        }
    }

    private void setupChartRT(LineChart chart){
        LineData data = new LineData();
        chart.setData(data);
        chart.calculateOffsets();
        chart.setBackgroundColor(Color.WHITE);
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawGridLines(true);
        x.setDrawAxisLine(true);
        x.setTextColor(Color.DKGRAY);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(4, true);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);

        YAxis y = chart.getAxisLeft();
        y.setDrawLabels(true);
        y.setLabelCount(10, true);
        y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(true);
        y.setDrawAxisLine(true);
        y.setValueFormatter(new YValueFormatter());

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
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
            data.addEntry(new Entry(set.getEntryCount(), waterLevel), 0);
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
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawFilled(false);
        set.setDrawCircles(false);
        set.setLineWidth(1.8f);
        set.setHighLightColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryVariant));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

}