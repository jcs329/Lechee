package com.quantatw.roomhub.manager.health.listener;

import com.quantatw.roomhub.manager.health.data.HealthData;

/**
 * Created by erin on 7/25/16.
 */
public interface ShareHealthDataListener {
    void addShareHealthData(HealthData healthData);
    void removeHealthData(HealthData healthData);
    void updateHealthData(HealthData healthData);
}
