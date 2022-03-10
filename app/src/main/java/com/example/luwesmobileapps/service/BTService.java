package com.example.luwesmobileapps.service;

import static com.example.luwesmobileapps.App.Channel_1_ID;
import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.example.luwesmobileapps.MainActivity;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BTService extends Service {
    private ConnectThread BTConnect;
    private ConnectedThread BTStream;
    private String myDeviceName;
    private SharedData DeviceData;
    private boolean isRunning;
    public NotificationManagerCompat myNotificationManager;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getMyDeviceName() {
        return myDeviceName;
    }

    public void setMyDeviceName(String myDeviceName) {
        this.myDeviceName = myDeviceName;
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
            BluetoothDevice mmDevice = intent.getParcelableExtra("Device Input");
            setMyDeviceName(mmDevice.getName());
            Intent notificationintent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationintent, 0);
            Notification notification = new NotificationCompat.Builder(this, Channel_1_ID)
                    .setContentTitle("Device Connection")
                    .setContentText("Trying to connect with " + getMyDeviceName())
                    .setSmallIcon(R.drawable.ic_bluetooth)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            BTConnect = new ConnectThread(mmDevice);
            BTConnect.start();
            if (BTConnect.isConnected()) {
                Notification notification2 = new NotificationCompat.Builder(this, Channel_1_ID)
                        .setContentTitle("Device Connection")
                        .setContentText("Connected with " + getMyDeviceName())
                        .setSmallIcon(R.drawable.ic_bluetooth)
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .build();
                myNotificationManager = NotificationManagerCompat.from(this);
                myNotificationManager.notify(1, notification2);
                setRunning(true);
                DeviceData.postConnectStatus(true);
            } else if (!BTConnect.isConnected())
                stopSelf();
        } else if (isRunning()) {
            String input = intent.getStringExtra("String Input");
            Log.d("Sent Data", input);
            BTStream.write(input.getBytes());
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceData.postConnectStatus(false);
        BTConnect.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "BT Service TAG";

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
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
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
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
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            BTStream = new BTService.ConnectedThread(mmSocket);
            BTStream.start();
            setConnectStatus(true);
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
                        handler.obtainMessage(LWRT,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("SET OK")||Payload.toString().contains("LWST")){
                        handler.obtainMessage(LWST,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("LWDL")){
                        handler.obtainMessage(LWDL,-1,-1,Payload.toString()).sendToTarget();
                    }
                    else if(Payload.toString().contains("LWDI")){
                        handler.obtainMessage(LWDI,-1,-1,Payload.toString()).sendToTarget();
                    }
                    Payload.delete(0, Payload.length());
                }
            }
        }

        Handler handler = new Handler(new Handler.Callback() {
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
                            write("LWST,7000000#\r\n".getBytes());
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
                        }
                        return true;
                    case LWDI:
                        String DeviceInfoString = (String) message.obj;
                        String[] splitString3 = DeviceInfoString.split(",");
                        DeviceData.postSiteName(getMyDeviceName());
                        DeviceData.postDeviceModel(splitString3[1]);
                        DeviceData.postFirmwareVersion(splitString3[2]);
                        if((splitString3[4].length()<5)){
                            DeviceData.postMACAddress("Not Registered");
                        }
                        else{
                            DeviceData.postMACAddress(splitString3[4].substring(0,15));
                        }
                        if(splitString3[5].length()<3){
                            DeviceData.postFirstRecord("No Records");
                        }else{
                            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/DDD");
                            Date FirstRecordDate = null;
                            try {
                                FirstRecordDate = sdf2.parse(splitString3[4]);
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
                        return true;
                }
                return false;
            }
        });

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
        }
    }
}
