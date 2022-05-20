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
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;
import com.example.luwesmobileapps.data_layer.FileAccess;
import com.example.luwesmobileapps.data_layer.SharedData;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BLEService extends Service {
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice mmDevice;
    private PendingIntent pendingIntent;
    private Intent notificationIntent;
    private String bluetoothAddress;
    private String payLoad;
    private SharedData deviceData;
    private HandlerThread BLEHandlerThread;
    private Handler BLEHandler;
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

    private String Characteristic_uuid_MESH = "0000ffe3-0000-1000-8000-00805f9b34fb";
    private String Characteristic_uuid_rx = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private String Characteristic_uuid_tx = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";

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
        deviceData = new SharedData();
        deviceData.postDownloadStatus(false);
        deviceData.postRealTimeStatus(false);
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
                    .setSmallIcon(R.drawable.ic_logo_luwes)
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
            }catch (Exception e){
                Log.d("Sent Data BLE", input);
                sendBLE(input+"\r\n");
//                if(input.contains("LWTS"))
//                    sendBLEMesh("F10101303248656C6C6F");
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                }else{
                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);
                }
                Notification notification2 = new NotificationCompat.Builder(getBaseContext(), Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Connected with " + mmDevice.getName())
                        .setSmallIcon(R.drawable.ic_logo_luwes)
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(getBaseContext());
                myNotificationManager.notify(1, notification2);
                deviceData.postMACAddress(mmDevice.getAddress());
                deviceData.postDeviceDataChanged(true);
                setRunning(true);
                BLEHandlerThread = new HandlerThread("BLEMessageHandler");
                BLEHandlerThread.start();

                BLEHandler = new Handler(BLEHandlerThread.getLooper(),new Handler.Callback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        switch (message.what){
                            case LWRT:
                                String RealTimeString = (String) message.obj;
                                String[] splitString1 = RealTimeString.split(",");
                                deviceData.postRecordStatus(splitString1[1]+"/"+splitString1[2]);
                                deviceData.postDeviceDateTime(splitString1[3]);
                                deviceData.postWaterLevel(splitString1[4]);
                                deviceData.postDeviceBattery(splitString1[5]);
                                if(splitString1.length>6) {
                                    if (Integer.parseInt(splitString1[6].substring(0, 1)) > 1) {
                                        deviceData.postInternetConnection("Connected");
                                    } else
                                        deviceData.postInternetConnection("Not connected");
                                }else{
                                    deviceData.postInternetConnection("Not connected");
                                }
                                deviceData.postDeviceDataChanged(true);
                                return true;
                            case LWST:
                                String SettingString = (String) message.obj;
                                if(SettingString.contains("SET OK")){
                                    sendBLE("LWST,7000000#\r\n");
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
                                    if (splitString2.length>7) {
                                        if (splitString2[7].contains("0"))
                                            deviceData.postMeasurementMode(0);
                                        else
                                            deviceData.postMeasurementMode(Integer.parseInt(splitString2[7]));
                                    }else
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
                                    deviceData.postDeviceDataChanged(true);
                                }
                                return true;
                            case LWDI:
                                String DeviceInfoString = (String) message.obj;
                                String[] splitString3 = DeviceInfoString.split(",");
                                deviceData.postSiteName(mmDevice.getName());
                                deviceData.postDeviceModel(splitString3[1]);
                                deviceData.postFirmwareVersion(splitString3[2]);
//                        if((splitString3[4].length()<5)){
//                            DeviceData.postMACAddress("Not Registered");
//                        }
//                        else{
//                            DeviceData.postMACAddress(splitString3[4].substring(0,15));
//                        }
                                deviceData.postMACAddress(mmDevice.getAddress());
                                DeviceData newDevice = new DeviceData(mmDevice.getAddress());
                                if(!SharedData.deviceList.contains(newDevice)) {
                                    newDevice.setDeviceName(mmDevice.getName());
                                    newDevice.setDeviceModel(splitString3[1]);
                                    newDevice.setDeviceConnection(2);
                                    newDevice.setLastConnection(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                                    SharedData.deviceList.add(newDevice);
                                    deviceData.postDeviceDataChanged(true);
                                    SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                                    Gson gson = new Gson();

                                    String jsonString = gson.toJson(SharedData.deviceList);

                                    prefsEditor.putString("deviceList", jsonString);
                                    prefsEditor.commit();
                                }
                                else{
                                    SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceName(mmDevice.getName());
                                    SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceModel(splitString3[1]);
                                    SharedData.deviceList.get(SharedData.deviceList.indexOf(newDevice)).setDeviceConnection(2);
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

                deviceData.postConnectStatus(2);
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.w(TAG,String.valueOf(STATE_DISCONNECTED));
                BLEHandlerThread.quitSafely();
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
                    setRXNotifier(gatt,mGattService, Characteristic_uuid_rx);
                    setRXNotifier(gatt,mGattService,Characteristic_uuid_MESH);
                    Log.d(TAG, "onServicesDiscovered: characteristic notification added");
                    sendBLE("LWST,7000000#\r\n");
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
                    String[] dupPayload = payLoad.split("\n");
//                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                    Log.d(TAG, "onCharacteristicRead: "+dupPayload.length);
                    if(dupPayload.length>1){
                        int x;
                        for(x=0;x<dupPayload.length;x++){
                            Log.d(TAG, "onCharacteristicRead: "+dupPayload[x]);
                            if(dupPayload[x].contains("LWRT")){
                                BLEHandler.obtainMessage(LWRT,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("SET OK")||dupPayload[x].contains("LWST")){
                                BLEHandler.obtainMessage(LWST,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("LWDL")){
                                BLEHandler.obtainMessage(LWDL,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("LWDI")){
                                BLEHandler.obtainMessage(LWDI,-1,-1,dupPayload[x]).sendToTarget();
                            }
                        }
                    }
                    else {
                        if (payLoad.contains("LWRT")) {
                            BLEHandler.obtainMessage(LWRT, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("SET OK") || payLoad.contains("LWST")) {
                            BLEHandler.obtainMessage(LWST, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("LWDL")) {
                            BLEHandler.obtainMessage(LWDL, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("LWDI")) {
                            BLEHandler.obtainMessage(LWDI, -1, -1, payLoad).sendToTarget();
                        }
                    }
                }
            }
            if (status == 0 && UUID.fromString(Characteristic_uuid_MESH).equals(characteristic.getUuid())){
                byte[] rawPayload = characteristic.getValue();
                Log.d(TAG, "onCharacteristicChanged: "+ new String(rawPayload));
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
                    String[] dupPayload = payLoad.split("\n");
//                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                    Log.d(TAG, "onCharacteristicChanged: "+dupPayload.length);
                    if(dupPayload.length>1){
                        int x;
                        for(x=0;x<dupPayload.length;x++){
                            Log.d(TAG, "onCharacteristicChanged: "+dupPayload[x]);
                            if(dupPayload[x].contains("LWRT")){
                                BLEHandler.obtainMessage(LWRT,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("SET OK")||dupPayload[x].contains("LWST")){
                                BLEHandler.obtainMessage(LWST,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("LWDL")){
                                BLEHandler.obtainMessage(LWDL,-1,-1,dupPayload[x]).sendToTarget();
                            }
                            else if(dupPayload[x].contains("LWDI")){
                                BLEHandler.obtainMessage(LWDI,-1,-1,dupPayload[x]).sendToTarget();
                            }
                        }
                    }
                    else {
                        if (payLoad.contains("LWRT")) {
                            BLEHandler.obtainMessage(LWRT, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("SET OK") || payLoad.contains("LWST")) {
                            BLEHandler.obtainMessage(LWST, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("LWDL")) {
                            BLEHandler.obtainMessage(LWDL, -1, -1, payLoad).sendToTarget();
                        } else if (payLoad.contains("LWDI")) {
                            BLEHandler.obtainMessage(LWDI, -1, -1, payLoad).sendToTarget();
                        }
                    }
                }
            }
            if (UUID.fromString(Characteristic_uuid_MESH).equals(characteristic.getUuid())){
                byte[] rawPayload = characteristic.getValue();
                Log.d(TAG, "onCharacteristicChanged: "+ new String(rawPayload));
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
            refresh.setAccessible(true);
            return ((Boolean) Objects.requireNonNull(refresh.invoke(gatt, new Object[0]))).booleanValue();
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
        deviceData.postConnectStatus(0);
        deviceData.postDownloadStatus(false);
        deviceData.postRealTimeStatus(false);
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
        BluetoothGattCharacteristic ShortMessage = bluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_tx));
        ShortMessage.setValue(string.getBytes());
        bluetoothGatt.writeCharacteristic(ShortMessage);
        delay(25);
    }

    @SuppressLint("MissingPermission")
    public void sendBLEMesh(String g) {
        BluetoothGattCharacteristic rxMeshChar = bluetoothGatt.getService(UUID.fromString(this.Service_uuid)).getCharacteristic(UUID.fromString(this.Characteristic_uuid_MESH));
        rxMeshChar.setValue(getBytesByString(g));
//        rxMeshChar.setValue(hex));
        bluetoothGatt.writeCharacteristic(rxMeshChar);
        delay(25);
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] getBytesByString(String data) {
        byte[] bytes = null;
        if (data != null) {
            String data2 = data.toUpperCase();
            int length = data2.length() / 2;
            char[] dataChars = data2.toCharArray();
            bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                bytes[i] = (byte) ((charToByte(dataChars[pos]) << 4) | charToByte(dataChars[pos + 1]));
            }
        }
        return bytes;
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
        String timeElapsed = new String();
        if(Hour>0){
            String.format(timeElapsed," %d hours %d minutes %d seconds",Hour,Minutes,Seconds);
        }else{
            if(Minutes>0){
                String.format(timeElapsed," %d minutes %d seconds",Minutes,Seconds);

            }else{
                String.format(timeElapsed," %d seconds",Seconds);
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
}
