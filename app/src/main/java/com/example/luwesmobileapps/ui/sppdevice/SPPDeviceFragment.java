package com.example.luwesmobileapps.ui.sppdevice;

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

public class SPPDeviceFragment extends Fragment {

    private SPPDeviceViewModel SPPDeviceViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SPPDeviceViewModel =
                ViewModelProviders.of(this).get(SPPDeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_sppdevice, container, false);
        final TextView textView = root.findViewById(R.id.DeviceName);
        SPPDeviceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}