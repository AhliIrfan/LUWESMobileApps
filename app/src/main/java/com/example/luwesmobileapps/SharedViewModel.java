package com.example.luwesmobileapps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> SiteName;
    private MutableLiveData<String> MACAddress;
    private MutableLiveData<String> FirmWareVersion;
    private MutableLiveData<String> DeviceModel;
    private MutableLiveData<String> DeviceDateTime;
    private MutableLiveData<String> FirstRecord;
    private MutableLiveData<String> LastRecord;
    private MutableLiveData<String> WaterLevel;
    private MutableLiveData<String> DeviceBattery;
    private MutableLiveData<String> IPAddress;
    private MutableLiveData<String> Port;
    private MutableLiveData<String> SensorZeroValues;
    private MutableLiveData<String> SensorOffset;
    private MutableLiveData<String> RecordInterval;
    private MutableLiveData<String> RecordStatus;
    private MutableLiveData<String> InternetConnectionStatus;

    public void setSiteName(String input){
        SiteName.setValue(input);
    }
    public void setFirstRecord(String input){
        FirstRecord.setValue(input);
    }
    public void setLastRecord(String input){
        LastRecord.setValue(input);
    }
    public void setMACAddress(String input){
        MACAddress.setValue(input);
    }
    public void setFirmWareVersion(String input){
        FirmWareVersion.setValue(input);
    }
    public void setDeviceModel(String input){
        DeviceModel.setValue(input);
    }
    public void setDeviceDateTime(String input){
        DeviceDateTime.setValue(input);
    }
    public void setWaterLevel(String input){
        WaterLevel.setValue(input);
    }
    public void setDeviceBattery(String input){
        DeviceBattery.setValue(input);
    }
    public void setIPAddress(String input){
        IPAddress.setValue(input);
    }
    public void setPort(String input){
        Port.setValue(input);
    }
    public void setSensorZeroValues(String input){
        SensorZeroValues.setValue(input);
    }
    public void setSensorOffset(String input){
        SensorOffset.setValue(input);
    }
    public void setRecordInterval(String input){
        RecordInterval.setValue(input);
    }
    public void setRecordStatus(String input){
        RecordStatus.setValue(input);
    }
    public void setInternetConnectionStatus(String input){
        InternetConnectionStatus.setValue(input);
    }


    public LiveData<String> getSiteName() {
        return SiteName;
    }
    public LiveData<String> getMACAddress() {
        return MACAddress;
    }
    public LiveData<String> getFirmWareVersion() {
        return FirmWareVersion;
    }
    public LiveData<String> getDeviceModel() {
        return DeviceModel;
    }
    public LiveData<String> getDeviceDateTime() {
        return DeviceDateTime;
    }
    public LiveData<String> getWaterLevel() {
        return WaterLevel;
    }
    public LiveData<String> getDeviceBattery() {
        return DeviceBattery;
    }
    public LiveData<String> getIPAddress() {
        return IPAddress;
    }
    public LiveData<String> getPort() {
        return Port;
    }
    public LiveData<String> getSensorZeroValues() {
        return SensorZeroValues;
    }
    public LiveData<String> getSensorOffset() {
        return SensorOffset;
    }
    public LiveData<String> getRecordInterval() {
        return RecordInterval;
    }
    public LiveData<String> getRecordStatus() {
        return RecordStatus;
    }
    public LiveData<String> getInternetConnectionStatus() {
        return InternetConnectionStatus;
    }

}
