package com.example.luwesmobileapps.service;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends Thread {
    public static final String TAG = "TCPService";
    private boolean jobCancelled = false;
    private String IP;
    private Integer port;
    private Socket TCPSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    public TCPClient(String serverIP, int port) {
        this.IP = serverIP;
        this.port = port;
    }

    @Override
    public void run() {
        int numBytes;
        StringBuilder Payload = new StringBuilder();
        try {
            InetAddress serverAddress = InetAddress.getByName(IP);
            TCPSocket = new Socket(serverAddress,port);
            try {
                mmInStream=TCPSocket.getInputStream();
                mmOutStream=TCPSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                Payload.delete(0, Payload.length());
            }
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
}
