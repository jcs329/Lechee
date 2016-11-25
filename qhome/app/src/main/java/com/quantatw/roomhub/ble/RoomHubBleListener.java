package com.quantatw.roomhub.ble;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by erin on 5/3/16.
 */
public interface RoomHubBleListener {
    void onLoadDeviceStart(RoomHubBLEController.JOB_TYPE workingType);
    void onLoadDevice(RoomHubBLEController.JOB_TYPE workingType, RoomHubBleDevice roomHubBleDevice);
    void onLoadDeviceDone(RoomHubBLEController.JOB_TYPE workingType);
}
