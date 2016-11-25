package com.quantatw.roomhub.manager.asset.listener;

import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.sls.device.CloudDevice;

/**
 * Created by 95010915 on 2015/9/23.
 */
public interface RoomHubChangeListener {
    public void addDevice(RoomHubData data);
    public void removeDevice(RoomHubData data);
    public void UpdateRoomHubData(int type,RoomHubData data);
    //public void UpdateRoomHubDeviceSeq(MicroLocationData locationData);
    public void UpdateDeviceShareUser(CloudDevice device);
    public void UpgradeStatus(String uuid,boolean is_upgrade);
}
