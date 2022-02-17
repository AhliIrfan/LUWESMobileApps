package com.example.luwesmobileapps;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Menu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public Activity MainActivity;
    final ArrayList<BluetoothDevice> btDeviceN = new ArrayList();
    myReceiver BTReceiver = new myReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        }

        MainActivity = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(BTReceiver,filter);

        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if(!bluetoothAdapter.isEnabled()){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }

                if(bluetoothAdapter.isEnabled()) {
                    final ArrayList btName = new ArrayList();
                    final ArrayList btAddress = new ArrayList();
                    final ArrayList<BluetoothDevice> btDevice = new ArrayList();
                    Set<BluetoothDevice> pairedDevices=bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            btName.add(device.getName());
                            btAddress.add(device.getAddress());
                            btDevice.add(device);
                        }
                    }
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity);
                    final DeviceListAdapter listAdapter = new DeviceListAdapter(MainActivity, btName, btAddress);
                    alertDialog.setTitle("Choose LUWES Data Logger Device");
                    alertDialog.setSingleChoiceItems(listAdapter, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent BTServiceIntent = new Intent(MainActivity, BluetoothService.class);
                            BTServiceIntent.putExtra("Device Input",btDevice.get(which));
                            ContextCompat.startForegroundService(MainActivity,BTServiceIntent);
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_spp, R.id.nav_ble)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public class myReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
                Intent ServiceIntent = new Intent(MainActivity,BluetoothService.class);
                stopService(ServiceIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BTReceiver);
    }
}
