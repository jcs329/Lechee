package com.quantatw.sls.listener;


import com.quantatw.sls.device.CloudDevice;

public interface  CloudDeviceListener {

	
	public  void addDevice(CloudDevice device);
	public  void removeDevice(CloudDevice device);
	
	
	public  void onlineDevice(CloudDevice device);
	public  void offlineDevice(CloudDevice device);


}
