package com.example.luwesmobileapps.data_layer;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileAccess {

    public void WriteDataToFile(String data, String DeviceName, String Year, String DoY){
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        File subRoot1 = new File(root, DeviceName);
        File subRoot2 = new File(subRoot1, "Record");
        File subRoot3 = new File(subRoot2, Year);
        if(!subRoot3.exists()){
            if(subRoot3.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        try{
            File gpxFile = new File(subRoot3, "DOY"+DoY+".csv");
            FileWriter writer = new FileWriter(gpxFile,true);
            if(data!=null) {
                writer.append(data);
                writer.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class Data{
        long timestamp;
        String payload;
        public Data(long timestamp, String payload) {
            this.timestamp = timestamp;
            this.payload = payload;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return timestamp == data.timestamp && payload.equals(data.payload);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, payload);
        }
    }

    public class plottingData{
        long timestamp;
        float waterLevel;
        float batteryPercentage;
        public plottingData(long timestamp, float waterLevel, float batteryPercentage) {
            this.timestamp = timestamp;
            this.waterLevel = waterLevel;
            this.batteryPercentage = batteryPercentage;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public float getWaterLevel() {
            return waterLevel;
        }

        public float getBatteryPercentage() {
            return batteryPercentage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            plottingData that = (plottingData) o;
            return timestamp == that.timestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp);
        }
    }

    class timeStampCompare implements Comparator<Data> {
        @Override
        public int compare(Data data, Data t1) {
            int status=0;
            if(data.timestamp>t1.timestamp)
                status = 1;
            else if(data.timestamp<t1.timestamp)
                status = -1;
            return status;
        }
    }

    public void BatchSort(String DeviceName, int StartYear,int StartDoY, int length){
        int yearCounter = StartYear;
        int dayCounter = StartDoY;
        for(int counter = 0; counter<length ;counter++){
            SortFile(DeviceName,String.valueOf(yearCounter),String.valueOf(dayCounter));
            dayCounter++;
            if(counter>365){
                yearCounter++;
                dayCounter=1;
            }
        }
    }

    public void SortFile(String DeviceName, String Year, String DoY) {
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        File subRoot1 = new File(root, DeviceName);
        File subRoot2 = new File(subRoot1, "Record");
        File subRoot3 = new File(subRoot2, Year);
        ArrayList<Data> TotalData = new ArrayList<>();
        if(!subRoot3.exists()){
            if(subRoot3.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        try{
            File gpxFile = new File(subRoot3, "DOY"+DoY+".csv");
            BufferedReader br
                    = new BufferedReader(new FileReader(gpxFile));
            String currentLine = br.readLine();
            while (currentLine != null) {
                String[] bufferData = currentLine.split(",");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date DateToConvert = null;
                if(bufferData[0]!=null){
                    try {
                        DateToConvert = sdf.parse(bufferData[0]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                assert DateToConvert != null;
                long timestamp = DateToConvert.getTime();
                String payload = bufferData[1] +","+ bufferData[2] +","+ bufferData[3];
                //Creating Student object for every student record and adding it to ArrayList
                Data bData = new Data(timestamp, payload);
                if(!TotalData.contains(bData)){
                    TotalData.add(bData);
                }
                currentLine = br.readLine();
            }

            Collections.sort(TotalData, new timeStampCompare());

            BufferedWriter bw = new BufferedWriter(new FileWriter(gpxFile));

            for(Data data : TotalData){
                bw.write(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.timestamp));
                bw.write(","+data.payload);
                try {
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            br.close();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void LoadPlotData(String DeviceName , ArrayList<plottingData> TotalData , Date startDate, Date endDate){
        int Year = Integer.parseInt(new SimpleDateFormat("yyyy").format(startDate));
        Date startFirstFayOfTheYear = null;
        try {
            startFirstFayOfTheYear = new SimpleDateFormat("dd/MM/yyyy").parse(1 + "/" + 1 + "/" + Year);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = endDate.getTime() - startDate.getTime();
        long diff2 = startDate.getTime() - startFirstFayOfTheYear.getTime();
        int DownloadLength = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
        int DoY = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;

        for(int i=0;i<DownloadLength;i++){
            File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
            File subRoot1 = new File(root, DeviceName);
            File subRoot2 = new File(subRoot1, "Record");
            File subRoot3 = new File(subRoot2, String.valueOf(Year));
            if(!subRoot3.exists()){
                if(subRoot3.mkdirs()){
                    Log.d("File Access", "WriteDataToFile: File created");
                }
                else
                    Log.d("File Access", "WriteDataToFile: Can't create file");
            }
            try {
                File gpxFile = new File(subRoot3, "DOY"+DoY+".csv");
                BufferedReader br
                        = new BufferedReader(new FileReader(gpxFile));
                String currentLine = br.readLine();
                while (currentLine != null) {
                    String[] bufferData = currentLine.split(",");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date DateToConvert = null;
                    if (bufferData[0] != null) {
                        try {
                            DateToConvert = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(bufferData[0]);
                        } catch (ParseException e) {
                            DateToConvert = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bufferData[0]);
                        }
                    }
                    assert DateToConvert != null;
                    long timestamp = DateToConvert.getTime();
                    if(DownloadLength>28)
                        timestamp = (timestamp/3600000)*3600000;
                    else
                        timestamp = (timestamp/60000)*60000;
                    Log.d("TAG", "LoadPlotData: "+ String.valueOf(timestamp));
                    float waterLevel = Float.parseFloat(bufferData[1]);
                    float batteryPercentage = Float.parseFloat(bufferData[3]);
                    //Creating Student object for every student record and adding it to ArrayList
                    plottingData bData = new plottingData(timestamp, waterLevel, batteryPercentage);
                    if (!TotalData.contains(bData))
                        TotalData.add(bData);
                    currentLine = br.readLine();
                }
                br.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            DoY++;
            if (DoY>365){
                DoY=1;
                Year++;
            }
        }

    }

    public void WriteDeviceList(String data){
        ArrayList<String> prevList = LoadDeviceList();
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        if(!root.exists()){
            if(root.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        String fileName = "DeviceList"+".txt";
        try{
            File gpxFile = new File(root, fileName);
            FileWriter writer = new FileWriter(gpxFile,true);
            if(data!=null && !prevList.contains(data)) {
                writer.append(data);
                writer.append("\n");
                writer.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> LoadDeviceList(){
        ArrayList<String> List = new ArrayList<>();
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        if(!root.exists()){
            if(root.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        String fileName = "DeviceList"+".txt";
        try{
            File gpxFile = new File(root, fileName);
            BufferedReader reader = new BufferedReader(new FileReader(gpxFile));
            String buffer = reader.readLine();
            while(buffer!=null){
                if(!List.contains(buffer))
                    List.add(buffer);
                buffer = reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return List;
    }
}
