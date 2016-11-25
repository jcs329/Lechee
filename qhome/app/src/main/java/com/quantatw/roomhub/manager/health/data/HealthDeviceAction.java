package com.quantatw.roomhub.manager.health.data;

/**
 * Created by erin on 5/10/16.
 */
public interface HealthDeviceAction {
    int rename(String newName);
    int update(HealthData healthData);
}
