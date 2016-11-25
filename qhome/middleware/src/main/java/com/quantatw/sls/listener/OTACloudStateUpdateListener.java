package com.quantatw.sls.listener;

import com.quantatw.sls.pack.roomhub.DeviceFirmwareUpdateStateResPack;

/**
 * Created by erin on 1/18/16.
 */
public interface OTACloudStateUpdateListener {
    public void stateChange(DeviceFirmwareUpdateStateResPack stateResPack);
}
