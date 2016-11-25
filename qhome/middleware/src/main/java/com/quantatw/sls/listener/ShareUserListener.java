package com.quantatw.sls.listener;

import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.pack.account.UserSharedDataResPack;

public interface ShareUserListener {
	public void addShareUser(CloudDevice device);
	public void removeShareUser(CloudDevice device);
	public void UserSharedData(UserSharedDataResPack userSharedData);
}
