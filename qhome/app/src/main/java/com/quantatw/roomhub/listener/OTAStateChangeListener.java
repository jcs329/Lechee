package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.manager.OTADevice;

/**
 * Created by erin on 1/18/16.
 */
public interface OTAStateChangeListener {
    public void checkVersionStart();
    public void checkVersionDone(OTADevice.NewVersionInfo newVersionInfo);
    public void upgradeStateChange(int upgradeState);
    public void upgradeStateChangeTimeout(int upgradeState);
}
