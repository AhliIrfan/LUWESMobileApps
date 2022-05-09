package com.example.luwesmobileapps.data_layer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private SharedData DeviceData;
    //Device Data Parameter//
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
    private MutableLiveData<String> LastLevel;
    private MutableLiveData<String> LastBattery;
    private MutableLiveData<String> LastConnection;
    //Device Logic Data//
    private MutableLiveData<Boolean> SettingStatus;
    private MutableLiveData<Boolean> SyncStatus;
    private MutableLiveData<Boolean> RealTimeStatus;
    private MutableLiveData<Boolean> DownloadStatus;
    private MutableLiveData<Boolean> DeviceDataChanged;
    private MutableLiveData<Boolean> BTPermission = new MutableLiveData<>();
    private MutableLiveData<Boolean> LocPermission = new MutableLiveData<>();
    private MutableLiveData<Boolean> FilePermission = new MutableLiveData<>();
    private MutableLiveData<Boolean> NightMode = new MutableLiveData<>();
    private MutableLiveData<Integer> GenerateData = new MutableLiveData<>();
    private MutableLiveData<Integer> ConnectStatus;

    public SharedViewModel() {
        DeviceData = new SharedData();
        //Device Data Parameter//
        SiteName = DeviceData.getSiteName();
        MACAddress = DeviceData.getMACAddress();
        FirmWareVersion = DeviceData.getFirmWareVersion();
        DeviceModel = DeviceData.getDeviceModel();
        DeviceDateTime = DeviceData.getDeviceDateTime();
        FirstRecord = DeviceData.getFirstRecord();
        LastRecord = DeviceData.getLastRecord();
        WaterLevel = DeviceData.getWaterLevel();
        DeviceBattery = DeviceData.getDeviceBattery();
        IPAddress = DeviceData.getIPAddress();
        Port = DeviceData.getPort();
        SensorZeroValues = DeviceData.getSensorZeroValues();
        SensorOffset = DeviceData.getSensorOffset();
        RecordInterval = DeviceData.getRecordInterval();
        RecordStatus = DeviceData.getRecordStatus();
        InternetConnectionStatus = DeviceData.getInternetConnectionStatus();
        //Device Logic Data//
        SettingStatus = SharedData.getSettingStatus();
        SyncStatus = SharedData.getSyncStatus();
        RealTimeStatus = SharedData.getRealTimeStatus();
        ConnectStatus = SharedData.getConnectStatus();
        DownloadStatus = SharedData.getDownloadStatus();
        DeviceDataChanged = SharedData.getDeviceDataChanged();
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

    public LiveData<String> getFirstRecord() {
        return FirstRecord;
    }

    public LiveData<String> getLastRecord() {
        return LastRecord;
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

    public LiveData<Boolean> getSettingStatus() {
        return SettingStatus;
    }

    public LiveData<Boolean> getSyncStatus() {
        return SyncStatus;
    }

    public LiveData<Boolean> getRealTimeStatus() {
        return RealTimeStatus;
    }

    public LiveData<Boolean> getBTPermission() {
        return BTPermission;
    }

    public LiveData<Boolean> getLocPermission() {
        return LocPermission;
    }

    public LiveData<Boolean> getFilePermission() {
        return FilePermission;
    }

    public LiveData<Boolean> getDownloadStatus() {
        return DownloadStatus;
    }

    public LiveData<Integer> getGenerateData() {
        return GenerateData;
    }

    public LiveData<Boolean> getDeviceDataChanged() {
        return DeviceDataChanged;
    }

    public LiveData<Boolean> getNightMode() {
        return NightMode;
    }

    public LiveData<Integer> getConnectStatus() {
        return ConnectStatus;
    }


    public void setSiteName(MutableLiveData<String> siteName) {
        SiteName = siteName;
    }

    public void setMACAddress(MutableLiveData<String> MACAddress) {
        this.MACAddress = MACAddress;
    }

    public void setFirmWareVersion(MutableLiveData<String> firmWareVersion) {
        FirmWareVersion = firmWareVersion;
    }

    public void setDeviceModel(MutableLiveData<String> deviceModel) {
        DeviceModel = deviceModel;
    }

    public void setDeviceDateTime(MutableLiveData<String> deviceDateTime) {
        DeviceDateTime = deviceDateTime;
    }

    public void setFirstRecord(MutableLiveData<String> firstRecord) {
        FirstRecord = firstRecord;
    }

    public void setLastRecord(MutableLiveData<String> lastRecord) {
        LastRecord = lastRecord;
    }

    public void setWaterLevel(MutableLiveData<String> waterLevel) {
        WaterLevel = waterLevel;
    }

    public void setDeviceBattery(MutableLiveData<String> deviceBattery) {
        DeviceBattery = deviceBattery;
    }

    public void setIPAddress(MutableLiveData<String> IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setPort(MutableLiveData<String> port) {
        Port = port;
    }

    public void setSensorZeroValues(MutableLiveData<String> sensorZeroValues) {
        SensorZeroValues = sensorZeroValues;
    }

    public void setSensorOffset(MutableLiveData<String> sensorOffset) {
        SensorOffset = sensorOffset;
    }

    public void setRecordInterval(MutableLiveData<String> recordInterval) {
        RecordInterval = recordInterval;
    }

    public void setRecordStatus(MutableLiveData<String> recordStatus) {
        RecordStatus = recordStatus;
    }

    public void setInternetConnectionStatus(MutableLiveData<String> internetConnectionStatus) {
        InternetConnectionStatus = internetConnectionStatus;
    }

    public void setLastConnection(String lastConnection) {
        LastConnection.setValue(lastConnection);
    }

    public void setSettingStatus(boolean settingStatus) {
        SettingStatus.setValue(settingStatus);
    }

    public void setSyncStatus(boolean syncStatus) {
        SyncStatus.setValue(syncStatus);
    }

    public void setRealTimeStatus(boolean realTimeStatus) {
        RealTimeStatus.setValue(realTimeStatus);
    }

    public void setBTPermission(boolean BTPermissionSet) {
        BTPermission.setValue(BTPermissionSet);
    }

    public void setLocPermission(boolean LocPermissionSet) {
        LocPermission.setValue(LocPermissionSet);
    }

    public void setFilePermission(boolean FilePermissionSet) {
        FilePermission.setValue(FilePermissionSet);
    }

    public void setNightMode(boolean nightMode) {
        NightMode.setValue(nightMode);
    }

    public void setDownloadStatus(boolean DownloadStatusSet) {
        DownloadStatus.setValue(DownloadStatusSet);
    }

    public void postGenerateData(int generateData) {
        GenerateData.postValue(generateData);
    }

    public void setGenerateData(Integer generateData) {
        GenerateData.setValue(generateData);
    }

    public void setDeviceDataChanged(boolean DeviceDataChangedSet) {
        DeviceDataChanged.setValue(DeviceDataChangedSet);
    }

    public void ClearAll(){
        SiteName.setValue("");
        MACAddress.setValue("");
        FirmWareVersion.setValue("");
        DeviceModel.setValue("");
        DeviceDateTime.setValue("");
        FirstRecord.setValue("");
        LastRecord.setValue("");
        WaterLevel.setValue("");
        DeviceBattery.setValue("");
        IPAddress.setValue("");
        Port.setValue("");
        SensorZeroValues.setValue("");
        SensorOffset.setValue("");
        RecordInterval.setValue("");
        RecordStatus.setValue("");
        InternetConnectionStatus.setValue("");
    }
}
