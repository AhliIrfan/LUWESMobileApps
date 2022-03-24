package com.example.luwesmobileapps.ui.setting;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingFragment extends Fragment {
    private fragmentListener listener;
    private SharedViewModel DeviceViewModel;
    private SwitchMaterial FilePermissionStat;
    private SwitchMaterial BTPermissionStat;
    private SwitchMaterial LocPermissionStat;


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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        BTPermissionStat = v.findViewById(R.id.bluetooth_permission_status);
        FilePermissionStat = v.findViewById(R.id.file_permission_status);
        LocPermissionStat = v.findViewById(R.id.location_permission_status);
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
        return v;
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