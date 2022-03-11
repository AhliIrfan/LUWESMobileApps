package com.example.luwesmobileapps.service;

import static com.example.luwesmobileapps.App.Channel_1_ID;
import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.luwesmobileapps.BuildConfig;
import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedData;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
    public static final String TAG = "BLEService";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public String Characteristic_uuid_rx = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public String Characteristic_uuid_tx = "0000ffe2-0000-1000-8000-00805f9b34fb";
    public String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";

    byte[] WriteBytes = new byte[2000];

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }


    class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
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
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
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
            Log.d("Sent Data", input);
        }
        return START_NOT_STICKY;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
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
                DeviceData.postConnectStatus(true);
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
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
                return;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: "+characteristic.getUuid());
            if (status == 0 && UUID.fromString(Characteristic_uuid_rx).equals(characteristic.getUuid())) {
                byte[] rawPayload = characteristic.getValue();
                Log.d(TAG, "onCharacteristicRead: "+rawPayload);
                if(rawPayload.length>0&&rawPayload!=null){
                    payLoad = new String(rawPayload);
                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged: "+characteristic.getUuid());
            if (UUID.fromString(Characteristic_uuid_rx).equals(characteristic.getUuid())) {
                byte[] rawPayload = characteristic.getValue();
                if(rawPayload.length>0&&rawPayload!=null){
                    payLoad = new String(rawPayload);
                    Log.d(TAG, "onCharacteristicRead: "+payLoad);
                    if(payLoad.equals("Hello World"));
                        sendBLE("Rise and Shine");
                }
            }
        }
    };



    public boolean initialize() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

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
        String str = bluetoothAddress;
        if (str == null || !address.equals(str) || bluetoothAddress == null) {
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
            connectionState= 1;
            return true;
        }
        Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
        if (!bluetoothGatt.connect()) {
            return false;
        }
        connectionState = 1;
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
        DeviceData.postConnectStatus(false);
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
        BluetoothGatt cBluetoothGatt;
        if (bluetoothAdapter == null || (cBluetoothGatt = bluetoothGatt) == null) {
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
}
