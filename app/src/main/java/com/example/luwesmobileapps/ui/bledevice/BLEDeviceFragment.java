package com.example.luwesmobileapps.ui.bledevice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.example.luwesmobileapps.R;

public class BLEDeviceFragment extends Fragment {

    private BLEDeviceViewModel BLEDeviceViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BLEDeviceViewModel =
                ViewModelProviders.of(this).get(BLEDeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bledevice, container, false);
        final TextView textView = root.findViewById(R.id.text_bledevice);
        BLEDeviceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}