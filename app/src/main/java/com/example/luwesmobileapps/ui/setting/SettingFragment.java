package com.example.luwesmobileapps.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luwesmobileapps.BuildConfig;
import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.DeviceData;
import com.example.luwesmobileapps.data_layer.SharedData;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.service.TCPClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingFragment extends Fragment {
    private SharedData.TCPSetting mTCPSetting;
    private fragmentListener listener;
    private TextView AppVersion;
    private SharedViewModel DeviceViewModel;
    private SwitchMaterial FilePermissionStat;
    private SwitchMaterial BTPermissionStat;
    private SwitchMaterial LocPermissionStat;
    private MaterialButton SaveTCPSetting;
    private EditText TCPIP;
    private EditText TCPPort;


    public interface fragmentListener{
        void checkBTPermission();
        void checkFilePermission();
        void checkLocPermission();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SettingFragment.fragmentListener) {
            listener = (SettingFragment.fragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement fragment listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener =null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Type TCPSetType = new TypeToken<SharedData.TCPSetting>(){}.getType();

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();

        String jsonString = appSharedPrefs.getString("TCPSetting", null);

        if(gson.fromJson(jsonString, TCPSetType)!=null) {
            mTCPSetting = gson.fromJson(jsonString, TCPSetType);
            TCPIP.setText(mTCPSetting.getIPAddress());
            TCPPort.setText(String.valueOf(mTCPSetting.getPort()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        SaveTCPSetting = v.findViewById(R.id.save);
        TCPIP = v.findViewById(R.id.TCPIP);
        TCPPort = v.findViewById(R.id.TCPPort);
        BTPermissionStat = v.findViewById(R.id.bluetooth_permission_status);
        FilePermissionStat = v.findViewById(R.id.file_permission_status);
        LocPermissionStat = v.findViewById(R.id.location_permission_status);
        AppVersion = v.findViewById(R.id.BuildVersion);



        DeviceViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        DeviceViewModel.getBTPermission().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                grantBT();
            }else{
                denyBT();
            }
        });
        DeviceViewModel.getLocPermission().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                grantLoc();
            }else{
                denyLoc();
            }
        });
        DeviceViewModel.getFilePermission().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                grantFile();
            }else{
                denyFile();
            }
        });

        BTPermissionStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkBTPermission();
            }
        });

        LocPermissionStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkLocPermission();
            }
        });

        FilePermissionStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.checkFilePermission();
            }
        });
        AppVersion.setText(BuildConfig.VERSION_NAME);

        SaveTCPSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTCPSetting = new SharedData.TCPSetting(TCPIP.getText().toString(),Integer.parseInt(TCPPort.getText().toString()));
                saveTCPSetting();
                Snackbar.make(requireContext(), requireView(), "TCP setting saved", Snackbar.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    public void saveTCPSetting(){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();

        String jsonString = gson.toJson(mTCPSetting);

        prefsEditor.putString("TCPSetting", jsonString);
        prefsEditor.commit();
    }



    public void grantBT(){
        BTPermissionStat.setChecked(true);
    }

    public void grantLoc(){
        LocPermissionStat.setChecked(true);
    }

    public void grantFile(){
        FilePermissionStat.setChecked(true);
    }

    public void denyBT(){
        BTPermissionStat.setChecked(false);
    }

    public void denyLoc(){
        LocPermissionStat.setChecked(false);
    }

    public void denyFile(){
        FilePermissionStat.setChecked(false);
    }
}