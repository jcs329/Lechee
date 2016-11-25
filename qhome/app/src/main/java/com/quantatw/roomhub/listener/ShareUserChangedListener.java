package com.quantatw.roomhub.listener;

import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.account.UserSharedDataResPack;

/**
 * Created by cherry.yang on 2016/05/17.
 */
public interface ShareUserChangedListener {
    public void AddShareUser(CloudDevice device);
    public void RemoveShareUser(CloudDevice device);
    public void UserSharedData(UserSharedDataResPack userSharedData);
}
