package com.example.luwesmobileapps.service;

import static com.example.luwesmobileapps.App.Channel_1_ID;
import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedData;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BLEService extends Service {
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice mmDevice;
    private PendingIntent pendingIntent;
    private Intent notificationIntent;
    private String bluetoothAddress;
    private String payLoad;
    private SharedData DeviceData;
    private boolean isRunning;
    private NotificationManagerCompat myNotificationManager;
    private final FileAccess myFileAccess = new FileAccess();

    int downloadCounter=0;
    int downloadLength=0;
    int startDoY=0;
    int startYear=0;
    long downloadStart=0;

    static final int LWRT=1;
    static final int LWST=2;
    static final int LWDL=3;
    static final int LWDI=4;

    public static final String TAG = "BLEService";

    public String Characteristic_uuid_rx = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public String Characteristic_uuid_tx = "0000ffe2-0000-1000-8000-00805f9b34fb";
    public String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;


    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceData = new SharedData();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning()) {
            mmDevice = intent.getParcelableExtra("Device Input");
            notificationIntent = new Intent(this, MainActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            }else{
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            }
            Notification notification = new NotificationCompat.Builder(this, Channel_1_ID)
                    .setContentTitle("Device Connection")
                    .setContentText("Trying to connect with " + mmDevice.getName())
                    .setSmallIcon(R.drawable.ic_bluetooth)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            connect(mmDevice.getAddress());
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
                myFileAccess.WriteDeviceList(DeviceData.getSiteName().getValue());
            }catch (Exception e){
                Log.d("Sent Data BLE", input);
                sendBLE(input);
            }
        }
        return START_NOT_STICKY;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.w(TAG,String.valueOf(STATE_CONNECTED));
                @SuppressLint("MissingPermission") Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Connected with " + mmDevice.getName())
                        .setSmallIcon(R.drawable.ic_bluetooth)
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                myNotificationManager.notify(1, notification2);
                setRunning(true);
                DeviceData.postConnectStatus(2);
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.w(TAG,String.valueOf(STATE_DISCONNECTED));
                stopSelf();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status  == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService mGattService = gatt.getService(UUID.fromString(Service_uuid));
                if(mGattService!=null) {
                    Log.d(TAG, "onServicesDiscovered: " + mGattService.getUuid().toString());
                    setRXNotifier(gatt,mGattService,Characteristic_uuid_rx);
                    Log.d(TAG, "onServicesDiscovered: characteristic notification added");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: "+characteristic.getUuid());
            if (status == 0 && UUID.fromString(Characteristic_uuid_rx).equals(characteristic.getUuid())) {
                byte[] rawPayload = characteristic.getValue();
                if(rawPayload.length>0){
                    payLoad = new String(rawPayload);
                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                    if(payLoad.contains("LWRT")){
                        handler.obtainMessage(LWRT,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("SET OK")||payLoad.contains("LWST")){
                        handler.obtainMessage(LWST,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("LWDL")){
                        handler.obtainMessage(LWDL,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("LWDI")){
                        handler.obtainMessage(LWDI,-1,-1,payLoad).sendToTarget();
                    }
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged: "+characteristic.getUuid());
            if (UUID.fromString(Characteristic_uuid_rx).equals(characteristic.getUuid())) {
                byte[] rawPayload = characteristic.getValue();
                if(rawPayload.length>0){
                    payLoad = new String(rawPayload);
                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                    if(payLoad.contains("LWRT")){
                        handler.obtainMessage(LWRT,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("SET OK")||payLoad.contains("LWST")){
                        handler.obtainMessage(LWST,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("LWDL")){
                        handler.obtainMessage(LWDL,-1,-1,payLoad).sendToTarget();
                    }
                    else if(payLoad.contains("LWDI")){
                        handler.obtainMessage(LWDI,-1,-1,payLoad).sendToTarget();
                    }
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("MissingPermission")
    public boolean connect(String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (!address.equals(bluetoothAddress)) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            if (Build.VERSION.SDK_INT >= 23) {
                bluetoothGatt = device.connectGatt(this, false, this.bluetoothGattCallback, 2);
            } else {
                bluetoothGatt = device.connectGatt(this, false, this.bluetoothGattCallback);
            }
            Log.d(TAG, "Trying to create a new connection.");
            bluetoothAddress = address;
            return true;
        }
        Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
        if (!bluetoothGatt.connect()) {
            return false;
        }
        return true;
    }

    private static boolean refreshGattCache(BluetoothGatt gatt) {
        if (gatt == null) {
            return false;
        }
        try {
            Method refresh = BluetoothGatt.class.getMethod("refresh", new Class[0]);
            if (refresh == null) {
                return false;
            }
            refresh.setAccessible(true);
            return ((Boolean) refresh.invoke(gatt, new Object[0])).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceData.postConnectStatus(0);
        disconnect();
    }

    @SuppressLint("MissingPermission")
    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothAddress = null;
        refreshGattCache(bluetoothGatt);
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /* access modifiers changed from: package-private */
    public void delay(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void setRXNotifier(BluetoothGatt gatt, BluetoothGattService service, String charUUID){
        BluetoothGattCharacteristic mGattCharacteristic = service.getCharacteristic(UUID.fromString(charUUID));
        gatt.setCharacteristicNotification(mGattCharacteristic, true);
        BluetoothGattDescriptor dsc = mGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        dsc.setValue(new byte[]{1, 0});
        gatt.writeDescriptor(dsc);
    }

    @SuppressLint("MissingPermission")
    public void sendBLE(String string) {
        BluetoothGattCharacteristic rxChar = bluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_tx));
        rxChar.setValue(string.getBytes());
        bluetoothGatt.writeCharacteristic(rxChar);
        delay(25);
    }


    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case LWRT:
                    String RealTimeString = (String) message.obj;
                    String[] splitString1 = RealTimeString.split(",");
                    DeviceData.postRecordStatus(splitString1[1]+"/"+splitString1[2]);
                    DeviceData.postDeviceDateTime(splitString1[3]);
                    DeviceData.postWaterLevel(splitString1[4]+" m");
                    DeviceData.postDeviceBattery(splitString1[5]+" v");
                    if (Integer.parseInt(splitString1[6].substring(0, 1)) > 1) {
                        DeviceData.postInternetConnection("Connected");
                    } else
                        DeviceData.postInternetConnection("Not connected");
                    return true;
                case LWST:
                    String SettingString = (String) message.obj;
                    DeviceData.postRealTimeStatus(false);
                    if(SettingString.contains("SET OK")){
                        sendBLE("LWST,7000000#\r\n");
                        DeviceData.postSettingStatus(false);
                    }
                    else if(SettingString.contains("SYNC OK")){
                        DeviceData.postSyncStatus(true);
                    }
                    else{
                        String[] splitString2 = SettingString.split(",");
                        DeviceData.postSiteName(splitString2[1]);
                        DeviceData.postIPAddress(splitString2[2]);
                        DeviceData.postPort(splitString2[3]);
                        DeviceData.postSensorOffset(splitString2[4]);
                        DeviceData.postSensorZeroValues(splitString2[5]);
                        DeviceData.postRecordInterval(splitString2[6]);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                        }else{
                            pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);
                        }
                        Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                                .setContentTitle("Device Connection")
                                .setContentText("Connected with " + DeviceData.getSiteName().getValue())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentIntent(pendingIntent)
                                .setOnlyAlertOnce(true)
                                .setOngoing(false)
                                .build();
                        myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                        myNotificationManager.notify(1, notification2);
                    }
                    return true;
                case LWDI:
                    String DeviceInfoString = (String) message.obj;
                    String[] splitString3 = DeviceInfoString.split(",");
                    DeviceData.postSiteName(mmDevice.getName());
                    DeviceData.postDeviceModel(splitString3[1]);
                    DeviceData.postFirmwareVersion(splitString3[2]);
//                    if((splitString3[4].length()<5)){
//                        DeviceData.postMACAddress("Not Registered");
//                    }
//                    else{
//                        DeviceData.postMACAddress(splitString3[4].substring(0,15));
//                    }
                    DeviceData.postMACAddress(mmDevice.getAddress());
                    if(splitString3[5].length()<3){
                        DeviceData.postFirstRecord("No Records");
                    }else{
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/DDD");
                        Date FirstRecordDate = null;
                        try {
                            FirstRecordDate = sdf2.parse(splitString3[5]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (FirstRecordDate != null) {
                            DeviceData.postFirstRecord(new SimpleDateFormat("dd/MM/yyyy").format(FirstRecordDate));
                        }
                    }
                    if(splitString3[6].length()<3){
                        DeviceData.postLastRecord("No Records");
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
                            DeviceData.postLastRecord(new SimpleDateFormat("dd/MM/yyyy").format(LastRecordDate));
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
                            myFileAccess.BatchSort(DeviceData.getSiteName().getValue(),startYear,startDoY,downloadLength);
                            downloadEndNotification();
                            downloadCounter=0;
                            DeviceData.postDownloadStatus(false);
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

                            long diff = dateData.getTime() - startFirstFayOfTheYear.getTime();
                            int DoY = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;

                            myFileAccess.WriteDataToFile(dataToSave, DeviceData.getSiteName().getValue(), year, String.valueOf(DoY));
                            downloadRunNotification(downloadCounter, downloadLength);
                        }
                    }
                    return true;
            }
            return false;
        }
    });

    public void downloadRunNotification(int progress,int max){
        int percentage = (int) ((Float.intBitsToFloat(downloadCounter)/Float.intBitsToFloat(downloadLength))*100);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_1_ID)
                .setSmallIcon(R.drawable.ic_devices)
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
                .setSmallIcon(R.drawable.ic_devices)
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
}
