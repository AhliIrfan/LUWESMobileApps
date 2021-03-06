package com.example.luwesmobileapps.data_layer;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class SharedData {
    //Device Data//
    private static MutableLiveData<String> SiteName = new MutableLiveData<>();
    private static MutableLiveData<String> MACAddress = new MutableLiveData<>();
    private static MutableLiveData<String> FirmWareVersion = new MutableLiveData<>();
    private static MutableLiveData<String> DeviceModel = new MutableLiveData<>();
    private static MutableLiveData<String> DeviceDateTime = new MutableLiveData<>();
    private static MutableLiveData<String> FirstRecord = new MutableLiveData<>();
    private static MutableLiveData<String> LastRecord = new MutableLiveData<>();
    private static MutableLiveData<String> WaterLevel = new MutableLiveData<>();
    private static MutableLiveData<String> DeviceBattery = new MutableLiveData<>();
    private static MutableLiveData<String> BatteryCapacity = new MutableLiveData<>();
    private static MutableLiveData<String> IPAddress = new MutableLiveData<>();
    private static MutableLiveData<String> Port = new MutableLiveData<>();
    private static MutableLiveData<String> SensorZeroValues = new MutableLiveData<>();
    private static MutableLiveData<String> SensorOffset = new MutableLiveData<>();
    private static MutableLiveData<String> RecordInterval = new MutableLiveData<>();
    private static MutableLiveData<String> RecordStatus = new MutableLiveData<>();
    private static MutableLiveData<String> InternetConnectionStatus = new MutableLiveData<>();
    private static MutableLiveData<Integer> MeasurementMode = new MutableLiveData<>();
    //Device Logic//
    private static MutableLiveData<Boolean> SettingStatus = new MutableLiveData<>();
    private static MutableLiveData<Boolean> SyncStatus= new MutableLiveData<>();
    private static MutableLiveData<Boolean> RealTimeStatus= new MutableLiveData<>();
    private static MutableLiveData<Boolean> DownloadStatus= new MutableLiveData<>();
    private static MutableLiveData<Boolean> DeviceDataChanged= new MutableLiveData<>();
    private static MutableLiveData<Integer> ConnectStatus= new MutableLiveData<>();
    private static MutableLiveData<Integer> GenerateData= new MutableLiveData<>();

    public static ArrayList<DeviceData> deviceList = new ArrayList<>();

    public void postSiteName(String string){
        SiteName.postValue(string);
    }
    public void postMACAddress(String string){
        MACAddress.postValue(string);
    }
    public void postFirmwareVersion(String string){
        FirmWareVersion.postValue(string);
    }
    public void postDeviceModel(String string){
        DeviceModel.postValue(string);
    }
    public void postDeviceDateTime(String string){
        DeviceDateTime.postValue(string);
    }
    public void postFirstRecord(String string){
        FirstRecord.postValue(string);
    }
    public void postLastRecord(String string){
        LastRecord.postValue(string);
    }
    public void postWaterLevel(String string){
        WaterLevel.postValue(string);
    }
    public void postDeviceBattery(String string){
        DeviceBattery.postValue(string);
    }
    public void postBatteryCapacity(String string){
        BatteryCapacity.postValue(string);
    }
    public void postIPAddress(String string){
        IPAddress.postValue(string);
    }
    public void postPort(String string){
        Port.postValue(string);
    }
    public void postSensorZeroValues(String string){
        SensorZeroValues.postValue(string);
    }
    public void postSensorOffset(String string){
        SensorOffset.postValue(string);
    }
    public void postRecordInterval(String string){
        RecordInterval.postValue(string);
    }
    public void postMeasurementMode(int input){
        MeasurementMode.postValue(input);
    }
    public void postRecordStatus(String string){
        RecordStatus.postValue(string);
    }
    public void postInternetConnection(String string){
        InternetConnectionStatus.postValue(string);
    }
    public void postSettingStatus(boolean input){
        SettingStatus.postValue(input);
    }
    public void postSyncStatus(boolean input){
        SyncStatus.postValue(input);
    }
    public void postRealTimeStatus(boolean input){
        RealTimeStatus.postValue(input);
    }
    public void postDownloadStatus(boolean input){
        DownloadStatus.postValue(input);
    }
    public void postDeviceDataChanged(boolean input){
        DeviceDataChanged.postValue(input);
    }
    public void postConnectStatus(int input){
        ConnectStatus.postValue(input);
    }
    public void postGenerateData(int input){
        GenerateData.postValue(input);
    }

    public MutableLiveData<String> getSiteName() {
        return SiteName;
    }

    public MutableLiveData<String> getMACAddress() {
        return MACAddress;
    }

    public MutableLiveData<String> getFirmWareVersion() {
        return FirmWareVersion;
    }

    public MutableLiveData<String> getDeviceModel() {
        return DeviceModel;
    }

    public MutableLiveData<String> getDeviceDateTime() {
        return DeviceDateTime;
    }

    public MutableLiveData<String> getFirstRecord() {
        return FirstRecord;
    }

    public MutableLiveData<String> getLastRecord() {
        return LastRecord;
    }

    public MutableLiveData<String> getWaterLevel() {
        return WaterLevel;
    }

    public MutableLiveData<String> getDeviceBattery() {
        return DeviceBattery;
    }

    public MutableLiveData<String> getBatteryCapacity() {
        return BatteryCapacity;
    }

    public MutableLiveData<String> getIPAddress() {
        return IPAddress;
    }

    public MutableLiveData<String> getPort() {
        return Port;
    }

    public MutableLiveData<String> getSensorZeroValues() {
        return SensorZeroValues;
    }

    public MutableLiveData<String> getSensorOffset() {
        return SensorOffset;
    }

    public MutableLiveData<String> getRecordInterval() {
        return RecordInterval;
    }

    public MutableLiveData<Integer> getMeasurementMode() {
        return MeasurementMode;
    }

    public MutableLiveData<String> getRecordStatus() {
        return RecordStatus;
    }

    public MutableLiveData<String> getInternetConnectionStatus() {
        return InternetConnectionStatus;
    }

    public MutableLiveData<Boolean> getSettingStatus() {
        return SettingStatus;
    }

    public MutableLiveData<Boolean> getSyncStatus() {
        return SyncStatus;
    }

    public MutableLiveData<Boolean> getRealTimeStatus() {
        return RealTimeStatus;
    }

    public MutableLiveData<Boolean> getDownloadStatus() {
        return DownloadStatus;
    }

    public MutableLiveData<Boolean> getDeviceDataChanged() {
        return DeviceDataChanged;
    }

    public MutableLiveData<Integer> getConnectStatus() {
        return ConnectStatus;
    }

    public MutableLiveData<Integer> getGenerateData() {
        return GenerateData;
    }

}
