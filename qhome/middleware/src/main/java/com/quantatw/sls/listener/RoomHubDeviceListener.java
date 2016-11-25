package com.quantatw.sls.listener;

import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ReasonType;

public interface  RoomHubDeviceListener {

    public  void addDevice(RoomHubDevice device,ReasonType reason);
    public  void removeDevice(RoomHubDevice device,ReasonType reason);
    public  void updateDevice(RoomHubDevice device);
    public  void switchNetwork(boolean connected);

}