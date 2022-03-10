package com.example.luwesmobileapps.ui.bledevice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.luwesmobileapps.R;

public class BLEDeviceFragment extends Fragment {

    private BLEDeviceViewModel BLEDeviceViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BLEDeviceViewModel = new ViewModelProvider(requireActivity()).get(BLEDeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bledevice, container, false);
        final TextView textView = root.findViewById(R.id.text_bledevice);
        BLEDeviceViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        return root;
    }
}