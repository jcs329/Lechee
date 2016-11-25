package com.quantatw.roomhub.ui.bpm;

import android.support.v4.app.Fragment;

import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;

/**
 * Created by erin on 5/27/16.
 */
public class BPMHistoryFragment extends Fragment implements HealthDeviceChangeListener {
    protected void refreshUpdate(int type, HealthData device) {
    }

    protected void refreshAdd(HealthData device) {
    }

    protected void refreshRemove(HealthData device) {
    }

    @Override
    public void updateDevice(int type, HealthData device) {
        refreshUpdate(type,device);
    }

    @Override
    public void addDeivce(HealthData device) {
        refreshAdd(device);
    }

    @Override
    public void removeDevice(HealthData device) {
        refreshRemove(device);
    }
}
