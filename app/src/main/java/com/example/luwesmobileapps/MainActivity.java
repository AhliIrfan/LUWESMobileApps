package com.example.luwesmobileapps;

import static com.example.luwesmobileapps.App.Channel_1_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.service.BLEService;
import com.example.luwesmobileapps.service.BTService;
import com.example.luwesmobileapps.ui.devicepage.DevicePageFragment;
import com.example.luwesmobileapps.ui.dialog.BLEScanDialog;
import com.example.luwesmobileapps.ui.dialog.BTScanDialog;
import com.example.luwesmobileapps.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements DevicePageFragment.fragmentListener, BTScanDialog.fragmentListener, HomeFragment.fragmentListener, BLEScanDialog.fragmentListener {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedViewModel DeviceViewModel;
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //BLE Param//
    private BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private BLEService bleService;
    private boolean scanning;
    private Handler handler = new Handler();
    private List<String> ScannedDeviceList = new ArrayList<>();
    private UUID ServiceUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private UUID CharacteristicUUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 65000;

    public Activity MainActivity;
    private NotificationManagerCompat myNotificationManager;
    private final actReceiver BTReceiver = new actReceiver();
    private BTScanDialog btScanDialog;
    private BLEScanDialog bleScanDialog;
    private FloatingActionButton fab;
    private boolean fabBT;
    private boolean fabLoc;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LinearLayout ScanAction = findViewById(R.id.connectmenu);
        fab = findViewById(R.id.fab);
        FloatingActionButton BTScan = findViewById(R.id.action_bluetoothscan);
        FloatingActionButton BLEScan = findViewById(R.id.action_blescan);
        FloatingActionButton Disconnect = findViewById(R.id.action_disconnect);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter4 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter5 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(BTReceiver,filter3);
        registerReceiver(BTReceiver,filter4);
        registerReceiver(BTReceiver,filter5);


        DeviceViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        if (Build.VERSION.SDK_INT >= 30) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                DeviceViewModel.setBTPermission(true);
            } else {
                DeviceViewModel.setBTPermission(false);
            }
        }else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                DeviceViewModel.setBTPermission(true);
            } else {
                DeviceViewModel.setBTPermission(false);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            DeviceViewModel.setLocPermission(true);
        }
        else {
            DeviceViewModel.setLocPermission(false);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            DeviceViewModel.setFilePermission(true);
        }
        else {
            DeviceViewModel.setFilePermission(false);
        }

        DeviceViewModel.getBTPermission().observe(this, aBoolean -> {
            if(aBoolean){
                fabBT=true;
                if(aBoolean&&fabLoc)
                    fab.setVisibility(View.VISIBLE);
            }else{
                fab.setVisibility(View.GONE);
            }
        });

        DeviceViewModel.getLocPermission().observe(this, aBoolean -> {
            if(aBoolean){
                fabLoc=true;
                if(aBoolean&&fabBT)
                    fab.setVisibility(View.VISIBLE);
            }else{
                fab.setVisibility(View.GONE);
            }
        });

        fab.setOnClickListener(view -> {
            if (fab.isExpanded()){
                fab.setExpanded(false);
                TransitionManager.beginDelayedTransition(ScanAction, new AutoTransition());
                ScanAction.setVisibility(View.INVISIBLE);
            }
            else{
                fab.setExpanded(true);
                TransitionManager.beginDelayedTransition(ScanAction, new AutoTransition());
                ScanAction.setVisibility(View.VISIBLE);
            }
        });

        BTScan.setOnClickListener(view -> {
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mStartForResult.launch(enableBtIntent);
            }

            if(bluetoothAdapter.isEnabled()) {
                BTScanDialog();
            }
            fab.setExpanded(false);
            TransitionManager.beginDelayedTransition(ScanAction, new AutoTransition());
            ScanAction.setVisibility(View.INVISIBLE);
        });

        BLEScan.setOnClickListener(view -> {
            ScannedDeviceList.clear();
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mStartForResult.launch(enableBtIntent);

            }

            if(bluetoothAdapter.isEnabled()) {
                BLEScanDialog();
            }
            fab.setExpanded(false);
            TransitionManager.beginDelayedTransition(ScanAction, new AutoTransition());
            ScanAction.setVisibility(View.INVISIBLE);
        });
        Disconnect.setOnClickListener(view -> {
            DeviceViewModel.ClearAll();
            Intent ServiceBT = new Intent(MainActivity, BTService.class);
            stopService(ServiceBT);
            Intent ServiceBLE = new Intent(MainActivity, BLEService.class);
            stopService(ServiceBLE);
            fab.setExpanded(false);
            TransitionManager.beginDelayedTransition(ScanAction, new AutoTransition());
            ScanAction.setVisibility(View.INVISIBLE);
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        Menu nav_menu = navigationView.getMenu();
        nav_menu.findItem(R.id.nav_devicepage).setEnabled(false);

        DeviceViewModel.getConnectStatus().observe(this, integer -> {
            Menu nav_menu1 = navigationView.getMenu();
            if(integer==0){
                nav_menu1.findItem(R.id.nav_devicepage).setEnabled(false);
            }
            else{
                nav_menu1.findItem(R.id.nav_devicepage).setEnabled(true);
            }
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_devicepage, R.id.nav_ble)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_appbar, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void BTStartScan() {
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void BTStopScan() {
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void BLEStartScan() {
        scanLeDevice();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void BLEStopScan() {
        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallback);
    }

    public class actReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName()!=null) {
                    Log.d("Device Discovery", device.getName());
                    btScanDialog.AddScannedDevice(device);
                }
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
                if(btScanDialog!=null)
                    btScanDialog.ScanButton(0,false);
                Log.d("Device Discovery","Start Discovering");
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                if(btScanDialog!=null)
                    btScanDialog.ScanButton(4,true);
                Log.d("Device Discovery","Stop Discovering");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BTReceiver);
    }

    public void BTScanDialog(){
        btScanDialog = new BTScanDialog();
        btScanDialog.show(getSupportFragmentManager(),"BTDialog");
    }

    public void BLEScanDialog(){
        bleScanDialog = new BLEScanDialog();
        bleScanDialog.show(getSupportFragmentManager(),"BLEDialog");
    }

    @Override
    public void BTSend(String string){
        Intent ServiceIntent = new Intent(MainActivity, BTService.class);
        ServiceIntent.putExtra("String Input",string);
        startService(ServiceIntent);
    }

    @Override
    public void BTStartDownload(int downloadLength, int startDoY, int startYear) {
        Intent ServiceIntent = new Intent(MainActivity, BTService.class);
        ServiceIntent.putExtra("String Input",String.valueOf(downloadLength));
        ServiceIntent.putExtra("String Input2",String.valueOf(startDoY));
        ServiceIntent.putExtra("String Input3",String.valueOf(startYear));
        startService(ServiceIntent);
    }

    @Override
    public void BLESend(String string){
        Intent ServiceIntent = new Intent(MainActivity, BLEService.class);
        ServiceIntent.putExtra("String Input",string);
        startService(ServiceIntent);
    }

    @Override
    public void BLEStartDownload(int downloadLength, int startDoY, int startYear) {
        Intent ServiceIntent = new Intent(MainActivity, BLEService.class);
        ServiceIntent.putExtra("String Input",String.valueOf(downloadLength));
        ServiceIntent.putExtra("String Input2",String.valueOf(startDoY));
        ServiceIntent.putExtra("String Input3",String.valueOf(startYear));
        startService(ServiceIntent);
    }

    @Override
    public void checkFilePermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        }
        else {
             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void checkLocPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,}, 2);
        }
    }

    @Override
    public void checkBTPermission(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,}, 3);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Result", "onRequestPermissionsResult: "+String.valueOf(requestCode));
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    DeviceViewModel.setFilePermission(true);
                    Log.d("Result", "onRequestPermissionsResult: File Granted");
                }else {
                    DeviceViewModel.setFilePermission(false);
                }
                break;
            case 2:
                if(grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    DeviceViewModel.setLocPermission(true);
                    Log.d("Result", "onRequestPermissionsResult: Loc Granted");
                }else {
                    DeviceViewModel.setLocPermission(false);
                }
                break;
            case 3:
                if(grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    DeviceViewModel.setBTPermission(true);
                    Log.d("Result", "onRequestPermissionsResult: BT Granted");
                }else {
                    DeviceViewModel.setBTPermission(false);
                }
                break;

        }
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    // Handle the Intent
                }
            });

    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    bluetoothLeScanner.stopScan(leScanCallback);
                    bleScanDialog.showProgressbar(false);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(scanFilters(),scanSetting(),leScanCallback);
            bleScanDialog.showProgressbar(true);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            bleScanDialog.showProgressbar(false);
        }
    }

    private ScanCallback leScanCallback =
            new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice ScannedDevice = result.getDevice();
                    if(!ScannedDeviceList.contains(ScannedDevice.getName())) {
                        bleScanDialog.AddScannedDevice(ScannedDevice);
                        ScannedDeviceList.add(ScannedDevice.getName());
                    }
                }
            };

    private List<ScanFilter> scanFilters() {
        List<ScanFilter> list = new ArrayList<>();

        ScanFilter scanFilterName = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(String.valueOf(ServiceUUID))).build();

        list.add(scanFilterName);

        return list;
    }

    private ScanSettings scanSetting() {
        ScanSettings setting = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            setting = new ScanSettings.Builder().setMatchMode(ScanSettings.CALLBACK_TYPE_FIRST_MATCH).build();
        }
        return setting;
    }
}
