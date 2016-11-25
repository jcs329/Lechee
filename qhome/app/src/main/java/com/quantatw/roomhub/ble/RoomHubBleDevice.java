package com.quantatw.roomhub.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by erin on 5/4/16.
 */
public class RoomHubBleDevice implements Parcelable {
    private BluetoothDevice bluetoothDevice;
    private String roomHubName;
    private String roomHubUuid;
    private int deviceBLEState = DeviceBLEState.IDLE;
    private int retryCount;
    private final int MAX_RETRY_TIMES = 2;

    private RoomHubBleDevice(Parcel in) {
        bluetoothDevice = in.readParcelable(this.getClass().getClassLoader());
        roomHubName = in.readString();
        roomHubUuid = in.readString();
        deviceBLEState = in.readInt();
        retryCount = in.readInt();
    }

    public static class DeviceBLEState {
        static final int IDLE = 0;
        static final int CONNECTING = 1;
        static final int CONNECTED = 2;
        static final int SERVICE_DISCOVERED = 3;
        static final int ENABLE_NOTIFY = 4;
        static final int WRITE = 5;
        static final int NOTIFIED = 6;
        static final int DISCONNECTED = 7;
    }

    public RoomHubBleDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getDeviceBLEState() {
        return deviceBLEState;
    }

    public void setDeviceBLEState(int deviceBLEState) {
        this.deviceBLEState = deviceBLEState;
        if(this.deviceBLEState < DeviceBLEState.DISCONNECTED)
            setRetryCount(0);
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getRoomHubName() {
        return roomHubName;
    }

    public void setRoomHubName(String roomHubName) {
        this.roomHubName = roomHubName;
    }

    public String getRoomHubUuid() {
        return roomHubUuid;
    }

    public void setRoomHubUuid(String roomHubUuid) {
        this.roomHubUuid = roomHubUuid;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isRetryFinished() {
        return this.retryCount == MAX_RETRY_TIMES?true:false;
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RoomHubBleDevice> CREATOR =
            new Parcelable.Creator<RoomHubBleDevice>() {
                public RoomHubBleDevice createFromParcel(Parcel in) {
                    return new RoomHubBleDevice(in);
                }
                public RoomHubBleDevice[] newArray(int size) {
                    return new RoomHubBleDevice[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bluetoothDevice, flags);
        dest.writeString(roomHubName);
        dest.writeString(roomHubUuid);
        dest.writeInt(deviceBLEState);
        dest.writeInt(retryCount);
    }
}
