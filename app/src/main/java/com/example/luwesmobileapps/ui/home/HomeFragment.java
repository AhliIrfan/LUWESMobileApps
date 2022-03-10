package com.example.luwesmobileapps.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.luwesmobileapps.R;
import com.example.luwesmobileapps.data_layer.SharedViewModel;
import com.example.luwesmobileapps.ui.devicepage.DevicePageFragment;

public class HomeFragment extends Fragment {
    private fragmentListener listener;
    private SharedViewModel DeviceViewModel;
    private TextView FilePermissionStat;
    private TextView BTPermissionStat;
    private TextView LocPermissionStat;
    private Button BTButton;
    private Button FileButton;
    private Button LocButton;


    public interface fragmentListener{
        void checkBTPermission();
        void checkFilePermission();
        void checkLocPermission();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragment.fragmentListener) {
            listener = (HomeFragment.fragmentListener) context;
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        BTButton = v.findViewById(R.id.bluetooth_permission_button);
        FileButton = v.findViewById(R.id.file_permission_button);
        LocButton = v.findViewById(R.id.location_permission_button);

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

        BTButton.setOnClickListener(view -> listener.checkBTPermission());
        FileButton.setOnClickListener(view -> listener.checkFilePermission());
        LocButton.setOnClickListener(view -> listener.checkLocPermission());


        return v;
    }

    public void grantBT(){
        BTPermissionStat.setText("Granted");
        BTPermissionStat.setTextColor(getResources().getColor(R.color.colorGranted));
    }

    public void grantLoc(){
        LocPermissionStat.setText("Granted");
        LocPermissionStat.setTextColor(getResources().getColor(R.color.colorGranted));
    }

    public void grantFile(){
        FilePermissionStat.setText("Granted");
        FilePermissionStat.setTextColor(getResources().getColor(R.color.colorGranted));
    }

    public void denyBT(){
        BTPermissionStat.setText("Denied");
        BTPermissionStat.setTextColor(getResources().getColor(R.color.colorError));
    }

    public void denyLoc(){
        LocPermissionStat.setText("Denied");
        LocPermissionStat.setTextColor(getResources().getColor(R.color.colorError));
    }

    public void denyFile(){
        FilePermissionStat.setText("Denied");
        FilePermissionStat.setTextColor(getResources().getColor(R.color.colorError));
    }
}