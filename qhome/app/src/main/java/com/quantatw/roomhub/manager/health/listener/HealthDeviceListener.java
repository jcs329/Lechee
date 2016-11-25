package com.quantatw.roomhub.manager.health.listener;

import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.sls.pack.base.BaseResPack;

/**
 * Created by erin on 4/28/16.
 */
public interface HealthDeviceListener {
    void addDeivce(HealthData device);
    void removeDevice(HealthData device);
    void updateDevice(int type, HealthData device);
    void contentChange(HealthData device, BaseResPack updateResPack);
}
