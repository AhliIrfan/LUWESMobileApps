package com.example.luwesmobileapps.data_layer;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class DeviceData {
    @SerializedName("deviceName")
    private String DeviceName;
    @SerializedName("deviceAddress")
    private String DeviceAddress;
    @SerializedName("deviceModel")
    private String DeviceModel;
    @SerializedName("deviceConnection")
    private int DeviceConnection;
    @SerializedName("lastConnection")
    private String LastConnection;
    @SerializedName("lastLevel")
    private String LastWaterLevel;
    @SerializedName("lastBattery")
    private String LastBattery;

    public DeviceData(String deviceAddress) {
        DeviceAddress = deviceAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceData that = (DeviceData) o;
        return DeviceAddress.equals(that.DeviceAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeviceAddress);
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getDeviceAddress() {
        return DeviceAddress;
    }

    public String getLastConnection() {
        return LastConnection;
    }

    public void setLastConnection(String lastConnection) {
        LastConnection = lastConnection;
    }

    public String getLastWaterLevel() {
        return LastWaterLevel;
    }

    public void setLastWaterLevel(String lastWaterLevel) {
        LastWaterLevel = lastWaterLevel;
    }

    public String getLastBattery() {
        return LastBattery;
    }

    public void setLastBattery(String lastBattery) {
        LastBattery = lastBattery;
    }

    public String getDeviceModel() {
        return DeviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        DeviceModel = deviceModel;
    }

    public int getDeviceConnection() {
        return DeviceConnection;
    }

    public void setDeviceConnection(int deviceConnection) {
        DeviceConnection = deviceConnection;
    }
}
