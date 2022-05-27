package com.example.luwesmobileapps.service;

import static com.example.luwesmobileapps.App.Channel_1_ID;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TCPClient extends Service {
    public static final String TAG = "TCPService";
    private boolean jobCancelled = false;
    private SharedData deviceData;
    private Socket TCPSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    public NotificationManagerCompat mNotificationManager;

    int uploadCounter =0;
    int uploadLength=0;
    int startDoY=0;
    int startYear=0;
    long uploadStart =0;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceData = new SharedData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotificationManager = NotificationManagerCompat.from(getBaseContext());
        String InputBuffer = intent.getStringExtra("String Input");
        String[] BufferArray = InputBuffer.split(",");
        Notification notification = new NotificationCompat.Builder(this, Channel_1_ID)
                .setContentTitle("Upload")
                .setContentText("Preparing to upload the files")
                .setSmallIcon(R.drawable.ic_logo_luwes)
                .build();
        startForeground(2, notification);
        Date startDate=null;
        Date endDate=null;
        try {
            startDate = new SimpleDateFormat("dd/MM/yyyy").parse(BufferArray[3]);
            endDate = new SimpleDateFormat("dd/MM/yyyy").parse(BufferArray[4]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate!=null||endDate!=null) {
            uploadStart =  new Date().getTime();
            UploadData mUpload = new UploadData(BufferArray[0],BufferArray[1],Integer.parseInt(BufferArray[2]),
                    startDate,endDate);
            mUpload.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void UploadFile(String DeviceName , String IP, Integer Port , Date startDate, Date endDate){
        InputStream mmInStream = null;
        OutputStream mmOutStream = null;
        String DeviceAddress = null;


        File rootA = new File(Environment.getExternalStorageDirectory(),"LUWESLogger");
        File subRoot1A = new File(rootA, DeviceName);
        if(!subRoot1A.exists()){
            if(subRoot1A.mkdirs()){
                Log.d("File Access", "WriteDataToFile: File created");
            }
            else
                Log.d("File Access", "WriteDataToFile: Can't create file");
        }
        try {
            File gpxFile = new File(subRoot1A, "Address.txt");
            BufferedReader br
                    = new BufferedReader(new FileReader(gpxFile));
            String Buffer = br.readLine();
            DeviceAddress = "000"+Buffer.replaceAll(":","");
        } catch (Exception e){
            e.printStackTrace();
        }


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
        uploadLength=DownloadLength;
        uploadRunNotification(uploadCounter,uploadLength);
        for(int i=0;i<DownloadLength;i++){
            int numBytes;
            StringBuilder Payload = new StringBuilder();
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
                    String TCPPayload = "0#"+DeviceName+"#"+DeviceAddress+"#"+bufferData2[1]+"#"+reDate+"#"+bufferData[1]+"#0,0,0#"+bufferData[2]+"#"+bufferData[3]+"\r\n";
                    Log.d("TAG", "UploadFile: "+TCPPayload);
                    //Creating Student object for every student record and adding it to ArrayList
                    mmOutStream.write(TCPPayload.getBytes());
                    int retryCounter = 0;
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
                            if(Payload.toString().contains(DeviceAddress)||Payload.toString().contains("Station")){
                                Payload.delete(0, Payload.length());
                                break;
                            }
                        }
                        else if(retryCounter>500){
                            Payload.delete(0, Payload.length());
                            break;
                        }
                        retryCounter++;
                    }
                    currentLine = br.readLine();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            DoY++;
            if (DoY>365){
                DoY=1;
                Year++;
            }
            uploadCounter++;
            uploadRunNotification(uploadCounter,uploadLength);
        }
        deviceData.postGenerateData(3);
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
            UploadFile(deviceName,IP,Port,startDate,endDate);
            deviceData.postGenerateData(3);
            uploadEndNotification();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void uploadRunNotification(int progress,int max){
        int percentage = (int) ((Float.intBitsToFloat(uploadCounter)/Float.intBitsToFloat(uploadLength))*100);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_1_ID)
                .setSmallIcon(R.drawable.ic_logo_luwes)
                .setContentTitle("Upload")
                .setContentText("Upload "+percentage+"%")
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(max,progress,false);
        mNotificationManager.notify(2,builder.build());
    }

    public void uploadEndNotification(){
        long duration = (new Date().getTime()- uploadStart)/1000;
        long Hour = duration/3600;
        long Minutes = (duration/60) % 60;
        long Seconds= duration % 60;
        String timeElapsed;
        if(Hour>0){
            timeElapsed = String.format(" %d hours %d minutes %d seconds",Hour,Minutes,Seconds);
        }else{
            if(Minutes>0){
                timeElapsed = String.format(" %d minutes %d seconds",Minutes,Seconds);

            }else{
                timeElapsed = String.format(" %d seconds",Seconds);
            }
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mNotificationManager.cancel(2);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_1_ID)
                .setSmallIcon(R.drawable.ic_logo_luwes)
                .setContentTitle("Upload")
                .setContentText("Upload complete")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your upload is complete, "+ uploadCounter +" of "+uploadLength+
                                " days records uploaded in"+timeElapsed))
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSound(defaultSoundUri)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mNotificationManager.notify(2, builder.build());
    }
}
