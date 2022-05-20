package com.example.luwesmobileapps.data_layer;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.luwesmobileapps.service.TCPClient;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileAccess {
    private Socket TCPSocket;

    public void SaveBatchFile(String DeviceName, String data, int FirstDOY,int LastDOY, int Year){
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        File subRoot1 = new File(root, DeviceName);
        File subRoot2 = new File(subRoot1, "Record");
        if(!subRoot2.exists()){
            if(subRoot2.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        try{
            File gpxFile = new File(subRoot2, DeviceName+Year+"DOY"+FirstDOY+"-"+LastDOY+".csv");
            FileWriter writer = new FileWriter(gpxFile,true);
            if(data!=null) {
                writer.append(data);
                writer.append("\n");
                writer.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

    private static class Data{
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

    public static class plottingData{
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

    public void LoadPlotData(String DeviceName , ArrayList<plottingData> TotalData , Date startDate, Date endDate, Boolean plot){
        int Year = Integer.parseInt(new SimpleDateFormat("yyyy").format(startDate));
        Log.d("TAG", "LoadPlotData: "+Year);
        Date startFirstDayOfTheYear = null;
        try {
            startFirstDayOfTheYear = new SimpleDateFormat("dd/MM/yyyy").parse(1 + "/" + 1 + "/" + Year);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = endDate.getTime() - startDate.getTime();
        assert startFirstDayOfTheYear != null;
        long diff2 = startDate.getTime() - startFirstDayOfTheYear.getTime();
        int DownloadLength = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
        int DoY = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;
        int FirstDoY = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;
        int LastDoY = FirstDoY+DownloadLength-1;
        if(!plot) {
            DeleteDuplicateFile(DeviceName, FirstDoY, LastDoY, Year);
        }
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
                    if(plot){
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
//                    Log.d("TAG", "LoadPlotData: "+ String.valueOf(timestamp));
                        float waterLevel = Float.parseFloat(bufferData[1]);
                        float batteryPercentage;
                        try {
                            batteryPercentage = Float.parseFloat(bufferData[3]);
                        } catch (NumberFormatException e) {
                            String[] buffer = bufferData[3].split("\"");
                            batteryPercentage = Float.parseFloat(buffer[0]);
                        }
                        //Creating Student object for every student record and adding it to ArrayList
                        plottingData bData = new plottingData(timestamp, waterLevel, batteryPercentage);
                        if (!TotalData.contains(bData))
                            TotalData.add(bData);
                    }
                    else {
                        SaveBatchFile(DeviceName,currentLine,FirstDoY,LastDoY,Year);
                        plottingData bData = new plottingData(DoY, 0, 0);
                        if (!TotalData.contains(bData))
                            TotalData.add(bData);
                    }
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
        File[] files = root.listFiles();
        if(files!=null) {
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                List.add(files[i].getName());
                Log.d("Files", "FileName:" + files[i].getName());
            }
        }
        Collections.sort(List, (s1, s2) -> s1.compareToIgnoreCase(s2));
        return List;
    }

    public static void DeleteDuplicateFile(String DeviceName,int FirstDOY,int LastDOY, int Year){
        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        File subRoot1 = new File(root, DeviceName);
        File subRoot2 = new File(subRoot1, "Record");
        if(!subRoot2.exists()){
            if(subRoot2.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        try{
            File gpxFile = new File(subRoot2, DeviceName+Year+"DOY"+FirstDOY+"-"+LastDOY+".csv");
            gpxFile.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void UploadFile(String DeviceName , String IP, Integer Port , Date startDate, Date endDate, ArrayList<plottingData> TotalData){
        InputStream mmInStream = null;
        OutputStream mmOutStream = null;

        int numBytes;
        StringBuilder Payload = new StringBuilder();

        try {
            InetAddress serverAddress = InetAddress.getByName(IP);
            TCPSocket = new Socket(serverAddress,Port);
            try {
                mmInStream=TCPSocket.getInputStream();
                mmOutStream=TCPSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int Year = Integer.parseInt(new SimpleDateFormat("yyyy").format(startDate));
        Log.d("TAG", "LoadPlotData: "+Year);
        Date startFirstDayOfTheYear = null;
        try {
            startFirstDayOfTheYear = new SimpleDateFormat("dd/MM/yyyy").parse(1 + "/" + 1 + "/" + Year);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = endDate.getTime() - startDate.getTime();
        assert startFirstDayOfTheYear != null;
        long diff2 = startDate.getTime() - startFirstDayOfTheYear.getTime();
        int DownloadLength = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
        int DoY = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;
        int FirstDoY = (int) TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS) + 1;
        int LastDoY = FirstDoY+DownloadLength-1;
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
                while (currentLine!=null){
                    String[] bufferData = currentLine.split(",");
                    String[] bufferData2 = bufferData[0].split(" ");
                    String reDate = new SimpleDateFormat("dd-MM-yyyy").format(
                            new SimpleDateFormat("dd/MM/yyyy").parse(bufferData2[0]));
                    String TCPPayload = "0#"+DeviceName+"#000113EE2A6D81C#"+bufferData2[1]+"#"+reDate+"#"+bufferData[1]+"#0,0,0#"+bufferData[2]+"#"+bufferData[3]+"\r\n";
                    Log.d("TAG", "UploadFile: "+TCPPayload);
                    //Creating Student object for every student record and adding it to ArrayList
                    mmOutStream.write(TCPPayload.getBytes());
                    while (true) {
                        try {
                            // Read from the InputStream.
                            numBytes = mmInStream.read();
                        } catch (IOException e) {
                            Log.d("TAG", "Input stream was disconnected", e);
                            break;
                        }
                        Payload.append((char) numBytes);
                        if (numBytes == '\n') {
                            //rawData = res.toString();
                            Log.d("Received Data", Payload.toString());
                            if(Payload.toString().contains("000113EE2A6D81C")||Payload.toString().contains("Station")){
                                Payload.delete(0, Payload.length());
                                break;
                            }
                        }
                    }
                    currentLine = br.readLine();
                    plottingData bData = new plottingData(DoY, 0, 0);
                    if (!TotalData.contains(bData))
                        TotalData.add(bData);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            DoY++;
            if (DoY>365){
                DoY=1;
                Year++;
            }
        }
        if (mmOutStream != null) {
            try {
                mmOutStream.flush();
                mmOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mmOutStream = null;
        mmInStream = null;
        try {
            TCPSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public void UploadFile(){
//        FTPClient ftpClient = new FTPClient();
//        try {
//            ftpClient.connect("175.158.47.234",3456);
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.login("ftp-luwes", "luwes123");
//            ftpClient.changeToParentDirectory();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        File root = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
//        File subRoot1 = new File(root, "Hellobejob");
//        File subRoot2 = new File(subRoot1, "Record");
//        if(!subRoot2.exists()){
//            if(subRoot2.mkdirs()){
//                Log.d("File Access", "WriteDataToFile: File created");
//            }
//            else
//                Log.d("File Access", "WriteDataToFile: Can't create file");
//        }
//        File gpxFile = new File(subRoot2, "Hellobejob2022DOY129-130.csv");
//        FileInputStream buffIn = null;
//        try {
//            buffIn = new FileInputStream(gpxFile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            ftpClient.storeFile("test.csv", buffIn);
//            Log.d("TAG", "UploadFile: file uploaded");
//            buffIn.close();
//            ftpClient.logout();
//            ftpClient.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public class TCPClient extends Thread {
        public static final String TAG = "TCPService";
        private boolean jobCancelled = false;
        private String IP;
        private Integer port;
        private Socket TCPSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public TCPClient(String serverIP, int port) {
            this.IP = serverIP;
            this.port = port;
        }

        @Override
        public void run() {
            int numBytes;
            StringBuilder Payload = new StringBuilder();
            try {
                InetAddress serverAddress = InetAddress.getByName(IP);
                TCPSocket = new Socket(serverAddress,port);
                try {
                    mmInStream=TCPSocket.getInputStream();
                    mmOutStream=TCPSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        public void sendTCPData(String payload){
            try {
                mmOutStream.write(payload.getBytes());
    //            Log.d(TAG, "sendTCPData: "+ payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void disconnect(){
            if (mmOutStream != null) {
                try {
                    mmOutStream.flush();
                    mmOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mmOutStream = null;
            mmInStream = null;
            try {
                TCPSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
