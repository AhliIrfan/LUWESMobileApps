package com.example.luwesmobileapps.ui.sppdevice;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SPPDeviceViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SPPDeviceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Promithevo-User");
    }

    public LiveData<String> getText() {
        return mText;
    }
}