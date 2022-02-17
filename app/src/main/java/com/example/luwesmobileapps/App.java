package com.example.luwesmobileapps;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String Channel_1_ID = "Application Notification";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
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
    }
}
