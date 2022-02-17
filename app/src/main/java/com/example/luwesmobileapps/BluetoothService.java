package com.example.luwesmobileapps;

import static com.example.luwesmobileapps.App.Channel_1_ID;
import static com.example.luwesmobileapps.MainActivity.bluetoothAdapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private ConnectThread BTConnect;
    private String myDeviceName;
    public final Handler myServiceHandler = new Handler();
    public NotificationManagerCompat myNotificationManager;

    public String getMyDeviceName() {
        return myDeviceName;
    }

    public void setMyDeviceName(String myDeviceName) {
        this.myDeviceName = myDeviceName;
    }

    @Override
    public void onCreate() {
    super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BluetoothDevice mmDevice =intent.getParcelableExtra("Device Input");
        setMyDeviceName(mmDevice.getName());
        Intent notificationintent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationintent,0 );
        Notification notification = new NotificationCompat.Builder(this,Channel_1_ID)
                .setContentTitle("Device Connection")
                .setContentText("Trying to connect with "+getMyDeviceName())
                .setSmallIcon(R.drawable.ic_menu_bluetooth)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
        BTConnect = new ConnectThread(mmDevice);
        if(BTConnect.isConnected()){
            Notification notification2 = new NotificationCompat.Builder(this,Channel_1_ID)
                    .setContentTitle("Device Connection")
                    .setContentText("Now connected with "+getMyDeviceName())
                    .setSmallIcon(R.drawable.ic_menu_bluetooth)
                    .setContentIntent(pendingIntent)
                    .build();
            myNotificationManager = NotificationManagerCompat.from(this);
                    myNotificationManager.notify(1,notification2);
        }
        else if(!BTConnect.isConnected())
            stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        private BluetoothService.ConnectedThread BTStream;
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
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            if(mmSocket.isConnected()) {
                BTStream = new BluetoothService.ConnectedThread(mmSocket);
                setConnectStatus(true);
            }
            else if(!mmSocket.isConnected()){
                myServiceHandler.post(new Runnable(){
                    public void run(){
                        stopSelf();
                    }
                });
                setConnectStatus(false);
            }
        }


        // Closes the client socket and causes the thread to finish.
        public void disconnect() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d("BT Connection","Could not close connection:" + e);
            }
        }
    }

    public class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private boolean ConnectStatus;

        public boolean isConnected() {
            return ConnectStatus;
        }

        public void setConnectStatus(boolean connectStatus) {
            ConnectStatus = connectStatus;
        }

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
            byte[] mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            StringBuilder Payload = new StringBuilder();

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    setConnectStatus(true);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
                Payload.append((char) numBytes);
                if (numBytes == '\n') {
                    //rawData = res.toString();
                    Log.d("RAWDATA", Payload.toString());
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
                        myServiceHandler.post(new Runnable(){
                            public void run(){
                                stopSelf();
                            }
                        });
                        return true;
                    case LWST:
                        String SettingString = (String) message.obj;
                        String[] splitString2 = SettingString.split(",");
                        return true;
                    case LWDI:
                        String DeviceInfoString = (String) message.obj;
                        String[] splitString3 = DeviceInfoString.split(",");
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
