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
import java.util.UUID;

public class BLEService extends Service {
    private BluetoothGatt bluetoothGatt;
    private String bluetoothAddress;
    private SharedData DeviceData;
    private boolean isRunning;
    private NotificationManagerCompat myNotificationManager;
    public static final String TAG = "BLEService";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public String Characteristic_uuid_TX = "0000fff1-0000-1000-8000-00805f9b34fb";
    public String Service_uuid = "0000ff0-0000-1000-8000-00805f9b34fb";

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

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                Log.w(TAG,String.valueOf(STATE_CONNECTED));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
                Log.w(TAG,String.valueOf(STATE_DISCONNECTED));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                return;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0 && UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid())) {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (UUID.fromString(Characteristic_uuid_TX).equals(characteristic.getUuid())) {
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceData = new SharedData();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning()) {
            BluetoothDevice mmDevice = intent.getParcelableExtra("Device Input");
            Intent notificationintent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationintent, 0);
            Notification notification = new NotificationCompat.Builder(this, Channel_1_ID)
                    .setContentTitle("Device Connection")
                    .setContentText("Trying to connect with " + mmDevice.getName())
                    .setSmallIcon(R.drawable.ic_bluetooth)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            connect(mmDevice.getAddress());
            if (connectionState==STATE_CONNECTED) {
                Notification notification2 = new NotificationCompat.Builder(this, Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Connected with " + mmDevice.getName())
                        .setSmallIcon(R.drawable.ic_bluetooth)
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(this);
                myNotificationManager.notify(1, notification2);
                setRunning(true);
                DeviceData.postConnectStatus(true);
            } else if (connectionState==STATE_DISCONNECTED)
                stopSelf();
        } else if (isRunning()) {
            String input = intent.getStringExtra("String Input");
            Log.d("Sent Data", input);
        }
        return START_NOT_STICKY;
    }

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

    public String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        for (int i = 0; i < bs.length; i++) {
            sb.append(digital[(bs[i] & 240) >> 4]);
            sb.append(digital[bs[i] & 15]);
        }
        return sb.toString();
    }

    public byte[] hex2byte(byte[] b) {
        if (b.length % 2 == 0) {
            byte[] b2 = new byte[(b.length / 2)];
            for (int n = 0; n < b.length; n += 2) {
                b2[n / 2] = (byte) Integer.parseInt(new String(b, n, 2), 16);
            }
            return b2;
        }
        throw new IllegalArgumentException("���Ȳ���ż��");
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
}
