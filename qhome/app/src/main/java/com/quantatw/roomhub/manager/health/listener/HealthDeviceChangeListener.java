package com.quantatw.roomhub.manager.health.listener;

import com.quantatw.roomhub.manager.health.data.HealthData;

/**
 * Created by erin on 4/28/16.
 */
public interface HealthDeviceChangeListener {
    void addDeivce(HealthData device);
    void removeDevice(HealthData device);
    void updateDevice(int type, HealthData device);
}
