package com.quantatw.sls.device;

import java.util.ArrayList;


public class CloudDeviceManagement {
	private ArrayList<CloudDevice> deviceList;

	
	static private String Tag = "DeviceManagement" ;
	public CloudDeviceManagement()
	{
		deviceList = new ArrayList<CloudDevice>();
		
	}
	
	public boolean AddDevice(CloudDevice dev)
	{
		boolean ret;
		synchronized(deviceList)
		{
			
			int index = indexOf(dev);

			if(index < 0)
			{
				ret = deviceList.add(dev);
			}
			else 
			{

				ret = true;
			}
			

				
		
			
		}
		return ret;
	}
	

	
	

	public int indexOf(CloudDevice dev)
	{
		int ret;
		synchronized(deviceList)
		{
			ret = deviceList.indexOf(dev);
		}
		return ret;
	}
	public CloudDevice Remove(int index)
	{
		CloudDevice ret;
		synchronized(deviceList)
		{
			ret = deviceList.remove(index);
		
		}
		return ret;
	}
	
	public boolean Remove(CloudDevice dev)
	{
		boolean ret;
		synchronized(deviceList)
		{
			ret = deviceList.remove(dev);
		
		}
		return ret;
	}
	public void RemoveAll()
	{
		synchronized(deviceList)
		{
			deviceList.clear();
		}

	}
	
	public CloudDevice Get(int index)
	{
		CloudDevice ret;
		synchronized(deviceList)
		{
			ret = deviceList.get(index);
		}
		return ret;
	}
	
	public CloudDevice GetOriginalDevice(CloudDevice dev)
	{
		CloudDevice ret;
		synchronized(deviceList)
		{
			int index = deviceList.indexOf(dev);
			if(index >= 0)
			{
				ret = deviceList.get(index);
			}
			else {
				ret = null;
			}
			
		}
		return ret;
	}
	
	
	public CloudDevice GetCloudDeviceFromAlljoyn(AlljoynDevice device)
	{
		CloudDevice ret;
		CloudDevice tempDevice = new CloudDevice();
		
		tempDevice.setUuid(device.getUuid());
		synchronized(deviceList)
		{
			int index = deviceList.indexOf(tempDevice);
			if(index >= 0)
			{
				ret = deviceList.get(index);
			}
			else {
				ret = null;
			}
			
		}
		return ret;
	}
	
	

	public void UpdateDevice(CloudDevice dev)
	{
		synchronized(deviceList)
		{
			int index = deviceList.indexOf(dev);
			if(index >= 0)
			{
				deviceList.get(index);
		
			}
		}
	}


	

	@SuppressWarnings("unchecked")
	public ArrayList<CloudDevice> GetClone()
	{
		ArrayList<CloudDevice> ret;
		synchronized(deviceList)
		{
			ret = (ArrayList<CloudDevice>) deviceList.clone();
			

		}
		return ret;
	}
}
