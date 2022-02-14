package com.example.luwesmobileapps.ui.bledevice;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class BLEDeviceViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BLEDeviceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}