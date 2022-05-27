package com.example.luwesmobileapps;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.luwesmobileapps.service.BTService;

public class App extends Application {
    public static final String Channel_1_ID = "Application Notification";
    private NotificationManagerCompat myNotificationManager;
    private final appReceiver BTReceiver = new appReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(BTReceiver,filter);
        registerReceiver(BTReceiver,filter2);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel Channel_1 = new NotificationChannel(
                    Channel_1_ID,
                    "Application Notification", NotificationManager.IMPORTANCE_HIGH
            );
            Channel_1.setDescription("Apps Related Notification");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(Channel_1);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(BTReceiver);
    }

    public class appReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
                Intent ServiceIntent = new Intent(getApplicationContext(), BTService.class);
                stopService(ServiceIntent);
                Notification notification2 = new NotificationCompat.Builder(getApplicationContext(),Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Last connection disconnected")
                        .setSmallIcon(R.drawable.ic_logo_luwes)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(getApplicationContext());
                myNotificationManager.cancelAll();
                myNotificationManager.notify(1,notification2);
                Log.d("TAG", "onReceive: Disconnect notification");
            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())){
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)==BluetoothAdapter.STATE_OFF) {
                    Intent ServiceIntent = new Intent(getApplicationContext(), BTService.class);
                    stopService(ServiceIntent);
                    Notification notification2 = new NotificationCompat.Builder(getApplicationContext(), Channel_1_ID)
                            .setContentTitle("Device Connection")
                            .setContentText("Bluetooth Turned Off")
                            .setSmallIcon(R.drawable.ic_logo_luwes)
                            .build();
                    myNotificationManager = NotificationManagerCompat.from(getApplicationContext());
                    myNotificationManager.cancelAll();
                    myNotificationManager.notify(1, notification2);
                }
            }
        }
    }
}
