package com.example.luwesmobileapps.ui.graphviewer;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GraphViewerFragment extends Fragment {

    private SharedViewModel BLEDeviceViewModel;
    private LineChart charts;
    private FileAccess myFileAccess = new FileAccess();
    private AutoCompleteTextView DeviceListOpt;
    private TextInputLayout StartDate;
    private TextInputLayout EndDate;
    private MaterialButton Generate;
    private ArrayList<FileAccess.plottingData> data = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BLEDeviceViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_graphviewer, container, false);
        DeviceListOpt = root.findViewById(R.id.selectDeviceContent);
        StartDate = root.findViewById(R.id.startDate);
        EndDate = root.findViewById(R.id.endDate);
        Generate = root.findViewById(R.id.Generate);

        ArrayAdapter<String> DeviceListAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.device_option,myFileAccess.LoadDeviceList()
        );

        StartDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        StartDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        EndDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.MyDateTimePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        EndDate.getEditText().setText(String.format("%d/%d/%d",dayOfMonth,monthOfYear+1,year));
                    }
                }, Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
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

        DeviceListOpt.setAdapter(DeviceListAdapter);
        charts = root.findViewById(R.id.lineChart);

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
        chart.getData().setHighlightEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setDrawLabels(true);
        x.setTextColor(Color.DKGRAY);
        x.setAvoidFirstLastClipping(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new XValueFormatter());

        YAxis y = chart.getAxisLeft();
        y.setDrawLabels(true);
        y.setLabelCount(10, false);
        y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setDrawAxisLine(false);
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
        public String getFormattedValue(float value) {
            String timestamp;
                timestamp = new SimpleDateFormat("dd MMM").format(new Date((long) value));
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
            set1.setCircleRadius(4f);
            set1.setCircleColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
            set1.setHighLightColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryVariant));
            set1.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
            set1.setDrawHorizontalHighlightIndicator(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            // create a data object with the data sets
            LineData dataL = new LineData(set1);
            dataL.setValueTextSize(9f);
            dataL.setDrawValues(false);

            // set data
            chart.setData(dataL);
        }
    }

}