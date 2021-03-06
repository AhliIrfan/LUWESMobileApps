package com.example.luwesmobileapps.service;

import static com.example.luwesmobileapps.App.Channel_1_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.preference.PreferenceManager;
import android.util.Log;

import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BTService extends Service {
    private final FileAccess myFileAccess = new FileAccess();
    private ConnectThread BTConnect;
    private ConnectedThread BTStream;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;
    private BluetoothDevice myDevice;
    private SharedData deviceData;
    private boolean isRunning;
    public NotificationManagerCompat myNotificationManager;

    int downloadCounter=0;
    int downloadLength=0;
    int startDoY=0;
    int startYear=0;
    long downloadStart=0;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        deviceData = new SharedData();
        deviceData.postConnectStatus(3);
        deviceData.postDownloadStatus(false);
        deviceData.postRealTimeStatus(false);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning()) {
            myDevice = intent.getParcelableExtra("Device Input");
            notificationIntent = new Intent(this, MainActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            }else{
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            }
            Notification notification = new NotificationCompat.Builder(this, Channel_1_ID)
                    .setContentTitle("Device Connection")
                    .setContentText("Trying to connect with " + myDevice.getName())
                    .setSmallIcon(R.drawable.ic_logo_luwes)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            BTConnect = new ConnectThread(myDevice);
            BTConnect.start();
        } else if (isRunning()) {
            String input = intent.getStringExtra("String Input");
            try{
                downloadLength= Integer.parseInt(input);

                String input2 = intent.getStringExtra("String Input2");
                String input3 = intent.getStringExtra("String Input3");
                startDoY= Integer.parseInt(input2);
                startYear= Integer.parseInt(input3);

                downloadStart= new Date().getTime();
                downloadRunNotification(downloadCounter,downloadLength);
            }catch (Exception e){
                Log.d("Sent Data BT", input);
                BTStream.write(input.getBytes());
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myNotificationManager!=null)
            myNotificationManager.cancelAll();
        deviceData.postDeviceDataChanged(false);
        deviceData.postRealTimeStatus(false);
        deviceData.postDownloadStatus(false);
        deviceData.postConnectStatus(0);
        BTConnect.disconnect();
        if(this.myDevice!=null)
            unpairDevice(myDevice);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "BT Service TAG";

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private boolean ConnectStatus = false;

        public boolean isConnected() {
            return ConnectStatus;
        }

        public void setConnectStatus(boolean connectStatus) {
            ConnectStatus = connectStatus;
        }

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
        // Cancel discovery because it otherwise slows down the connection.
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("BT Connection", "Could not close the client socket", closeException);
                }
                setConnectStatus(false);
                Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Connection timeout")
                        .setSmallIcon(R.drawable.ic_logo_luwes)
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                myNotificationManager.notify(1, notification2);
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        stopSelf();
                    }
                }, 2000);
                return;
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            BTStream = new BTService.ConnectedThread(mmSocket);
            BTStream.start();

            setRunning(true);
            BTStream.write("LWST,7000000#\r\n".getBytes());
        }


        // Closes the client socket and causes the thread to finish.
        public void disconnect() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d("BT Connection","Could not close connection:" + e);
            }
            if(BTStream!=null)
                BTStream.cancel();
        }
    }

    public class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private HandlerThread BTMessageHandler;
        private Handler BTHandler;

        static final int LWRT=1;
        static final int LWST=2;
        static final int LWDL=3;
        static final int LWDI=4;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            BTMessageHandler= new HandlerThread("BTMessageHandler");
            BTMessageHandler.start();

            BTHandler = new Handler(BTMessageHandler.getLooper(),new Handler.Callback() {
                @SuppressLint("MissingPermission")
                @Override
                public boolean handleMessage(@NonNull Message message) {
                    switch (message.what){
                        case LWRT:
                            String RealTimeString = (String) message.obj;
                            String[] splitString1 = RealTimeString.split(",");
                            deviceData.postRecordStatus(splitString1[1]+" | "+splitString1[2]);
                            deviceData.postDeviceDateTime(splitString1[3]);
                            deviceData.postWaterLevel(splitString1[4]);
                            float batteryVoltage = Float.parseFloat(splitString1[5]);
                            deviceData.postDeviceBattery(String.format("%.2f",batteryVoltage)+"V");
                            float batteryPercentage = (float) (100 - ((4.20-Float.parseFloat(splitString1[5]))/0.0075));
                            deviceData.postBatteryCapacity(String.valueOf(batteryPercentage));
                            if (Integer.parseInt(splitString1[6].substring(0, 1)) > 1)
                                deviceData.postInternetConnection("Connected");
                            else
                                deviceData.postInternetConnection("Not connected");
                            deviceData.postDeviceDataChanged(true);
                            return true;
                        case LWST:
                            String SettingString = (String) message.obj;
                            if(SettingString.contains("SET OK")){
                                delay(200);
                                write("LWST,7000000#\r\n".getBytes());
                                deviceData.postSettingStatus(false);
                            }
                            else if(SettingString.contains("SYNC OK")){
                                deviceData.postSyncStatus(true);
                            }
                            else{
                                deviceData.postRealTimeStatus(false);
                                String[] splitString2 = SettingString.split(",");
                                deviceData.postSiteName(splitString2[1]);
                                deviceData.postIPAddress(splitString2[2]);
                                deviceData.postPort(splitString2[3]);
                                deviceData.postSensorOffset(splitString2[4]);
                                deviceData.postSensorZeroValues(splitString2[5]);
                                deviceData.postRecordInterval(splitString2[6]);
                                String[] splitString3 = splitString2[7].split("\r");
                                if (splitString2.length>7)
                                    deviceData.postMeasurementMode(Integer.parseInt(splitString3[0]));
                                else
                                    deviceData.postMeasurementMode(0);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                                }else{
                                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);
                                }
                                Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                                        .setContentTitle("Device Connection")
                                        .setContentText("Connected with " + deviceData.getSiteName().getValue())
                                        .setSmallIcon(R.drawable.ic_logo_luwes)
                                        .setContentIntent(pendingIntent)
                                        .setOnlyAlertOnce(true)
                                        .setOngoing(false)
                                        .build();
                                myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                                myNotificationManager.notify(1, notification2);
                                myFileAccess.WriteDeviceAddress(deviceData.getMACAddress().getValue(),deviceData.getSiteName().getValue());
                                deviceData.postDeviceDataChanged(true);
                            }
                            return true;
                        case LWDI:
                            Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                                    .setContentTitle("Device Connection")
                                    .setContentText("Connected with " + myDevice.getName())
                                    .setSmallIcon(R.drawable.ic_logo_luwes)
                                    .setContentIntent(pendingIntent)
                                    .setOngoing(false)
                                    .build();
                            myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                            myNotificationManager.notify(1, notification2);
                            deviceData.postConnectStatus(1);

                            String DeviceInfoString = (String) message.obj;
                            String[] splitString3 = DeviceInfoString.split(",");
                            deviceData.postSiteName(myDevice.getName());
                            deviceData.postDeviceModel(splitString3[1]);
                            deviceData.postFirmwareVersion(splitString3[2]);
//                        if((splitString3[4].length()<5)){
//                            DeviceData.postMACAddress("Not Registered");
//                        }
//                        else{
//                            DeviceData.postMACAddress(splitString3[4].substring(0,15));
//                        }
                            deviceData.postMACAddress(myDevice.getAddress());
                            DeviceData newDevice = new DeviceData(myDevice.getAddress());
                            if(!SharedData.deviceList.contains(newDevice)) {
                                newDevice.setDeviceName(myDevice.getName());
                                newDevice.setDeviceModel(splitString3[1]);
                                newDevice.setDeviceConnection(1);
                                newDevice.setLastConnection(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                                SharedData.deviceList.add(newDevice);
                                SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                                Gson gson = new Gson();

                                String jsonString = gson.toJson(SharedData.deviceList);

                                prefsEditor.putString("deviceList", jsonString);
                                prefsEditor.commit();
                            }
                            else{
                                SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceName(myDevice.getName());
                                SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceModel(splitString3[1]);
                                SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceConnection(1);
                                SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setLastConnection(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                                SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                                Gson gson = new Gson();

                                String jsonString = gson.toJson(SharedData.deviceList);

                                prefsEditor.putString("deviceList", jsonString);
                                prefsEditor.commit();
                            }
                            if(splitString3[5].length()<3){
                                deviceData.postFirstRecord("No Records");
                            }else{
                                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/DDD");
                                Date FirstRecordDate = null;
                                try {
                                    FirstRecordDate = sdf2.parse(splitString3[5]);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (FirstRecordDate != null) {
                                    deviceData.postFirstRecord(new SimpleDateFormat("dd/MM/yyyy").format(FirstRecordDate));
                                }
                            }
                            if(splitString3[6].length()<3){
                                deviceData.postLastRecord("No Records");
                            }else{
                                String[] splitString4 = splitString3[6].split("/r");
                                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/DDD");
                                Date LastRecordDate = null;
                                try {
                                    LastRecordDate = sdf2.parse(splitString4[0]);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (LastRecordDate != null) {
                                    deviceData.postLastRecord(new SimpleDateFormat("dd/MM/yyyy").format(LastRecordDate));
                                }
                            }

                            return true;
                        case LWDL:
                            String DownloadString = (String) message.obj;
                            String[] splitString5 = DownloadString.split(",");
                            if(splitString5.length==2){
                                String[] splitString6;
                                splitString6 = splitString5[1].split("\r");
                                if(splitString6[0].contains("000")){
                                    myFileAccess.BatchSort(deviceData.getSiteName().getValue(),startYear,startDoY,downloadLength);
                                    downloadEndNotification();
                                    downloadCounter=0;
                                    deviceData.postDownloadStatus(false);
                                }
                            }
                            else if(splitString5.length==4) {
                                downloadCounter++;
                                downloadRunNotification(downloadCounter,downloadLength);
                            }
                            else{
                                String[] splitString7;
                                splitString7 = splitString5[2].split("#");
                                String dataToSave = splitString7[0]+" "+splitString7[1]+
                                        ","+splitString7[2]+","+splitString7[3]+","+splitString7[4];

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Date dateData = null;
                                try {
                                    dateData = sdf.parse(splitString7[0]);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(dateData!=null) {

                                    String year = new SimpleDateFormat("yyyy").format(dateData);

                                    Date startFirstFayOfTheYear = null;
                                    try {
                                        startFirstFayOfTheYear = sdf.parse(1 + "/" + 1 + "/" + year);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    long diff = dateData.getTime() - Objects.requireNonNull(startFirstFayOfTheYear).getTime();
                                    int DoY = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;

                                    myFileAccess.WriteDataToFile(dataToSave, deviceData.getSiteName().getValue(), year, String.valueOf(DoY));
                                    downloadRunNotification(downloadCounter, downloadLength);
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        public void run() {
            // mmBuffer store for the stream
            int numBytes; // bytes returned from read()
            StringBuilder Payload = new StringBuilder();

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
                Payload.append((char) numBytes);
                if (numBytes == '\n') {
                    //rawData = res.toString();
                    Log.d("Received Data", Payload.toString());
                    if(Payload.toString().contains("LWRT")){
                        BTHandler.obtainMessage(LWRT,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("SET OK")||Payload.toString().contains("LWST")){
                        BTHandler.obtainMessage(LWST,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("LWDL")){
                        BTHandler.obtainMessage(LWDL,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("LWDI")){
                        BTHandler.obtainMessage(LWDI,-1,-1,Payload.toString()).sendToTarget();
                    }
                    Payload.delete(0, Payload.length());
                }
            }
        }



        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                    mmInStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mmOutStream!= null) {
                try {
                    mmOutStream.close();
                    mmOutStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BTMessageHandler.quitSafely();
        }
    }

    public void downloadRunNotification(int progress,int max){
        int percentage = (int) ((Float.intBitsToFloat(downloadCounter)/Float.intBitsToFloat(downloadLength))*100);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_1_ID)
                .setSmallIcon(R.drawable.ic_logo_luwes)
                .setContentTitle("Download")
                .setContentText("Download progress "+percentage+"%")
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(max,progress,false);
        myNotificationManager.notify(2,builder.build());
    }

    public void downloadEndNotification(){
        long duration = (new Date().getTime()-downloadStart)/1000;
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
        myNotificationManager.cancel(2);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_1_ID)
                .setSmallIcon(R.drawable.ic_logo_luwes)
                .setContentTitle("Download")
                .setContentText("Download complete")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your download is complete, "+downloadCounter+" of "+downloadLength+
                                " days records downloaded in"+timeElapsed))
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSound(defaultSoundUri)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        myNotificationManager.notify(3, builder.build());
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void delay(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
