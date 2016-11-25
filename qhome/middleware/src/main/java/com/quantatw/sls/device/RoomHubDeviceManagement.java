package com.quantatw.sls.device;

import java.util.ArrayList;

import android.R.bool;
import android.R.integer;
import android.nfc.Tag;
import android.util.Log;


public class RoomHubDeviceManagement {
    private ArrayList<RoomHubDevice> deviceList;

    static private String Tag = "RoomHubDeviceManagement" ;
    public RoomHubDeviceManagement()
    {
        deviceList = new ArrayList<RoomHubDevice>();
    }

    public boolean AddAlljoynDevice(RoomHubDevice dev)
    {
        boolean ret;
        synchronized(deviceList)
        {

            int index = indexOfAlljoynDevice(dev);

            if(index < 0)
            {
                ret = deviceList.add(dev);
            }
            else
            {
                deviceList.get(index).setSessionid(dev.getSessionid());
                deviceList.get(index).setName(dev.getName());
                deviceList.get(index).setRoomHubInterface(dev.getRoomHubInterface());
                deviceList.get(index).setConfigCtrlInterface(dev.getConfigCtrlInterface());
//				deviceList.get(index).setUuid(dev.getUuid());
                ret = true;
            }
        }
        return ret;
    }

    public RoomHubDevice RemoveAlljoynDevice(RoomHubDevice dev)
    {
        RoomHubDevice ret = null;

        synchronized(deviceList)
        {
            int index = deviceList.indexOf(dev);
            if(index >= 0)
            {
                ret = deviceList.remove(index);

            }
        }
        return ret;
    }

    public int indexOfAlljoynDevice(RoomHubDevice dev)
    {
        int ret;
        synchronized(deviceList)
        {
            ret = deviceList.indexOf(dev);
        }
        return ret;
    }

    public RoomHubDevice RemoveAlljoynDevice(int index)
    {
        RoomHubDevice ret = null;
        synchronized(deviceList)
        {
            if(index <deviceList.size())
            {
                ret = deviceList.remove(index);

            }
        }
        return ret;
    }

    public boolean RemoveAlljoynDevice(AlljoynDevice dev)
    {
        boolean ret;
        synchronized(deviceList)
        {
            ret = deviceList.remove(dev);

        }
        return ret;
    }

    public boolean CheckCloudDeviceInAlljoyn(CloudDevice dev)
    {
        boolean ret;
        synchronized(deviceList)
        {
            int index = 0;
            RoomHubDevice device = new RoomHubDevice();
            device.setUuid(dev.getUuid());
            index = deviceList.indexOf(device);
            if(index >= 0)
            {
                ret = true;
            }
            else {
                ret = false;
            }
        }
        return ret;
    }

    public RoomHubDevice getAlljoynDeviceFromCloudDevice(CloudDevice dev)
    {
        RoomHubDevice returnDevice = null;
        synchronized(deviceList)
        {
            int index = 0;
            RoomHubDevice device = new RoomHubDevice();
            device.setUuid(dev.getUuid());
            index = deviceList.indexOf(device);
            if(index >= 0)
            {
                returnDevice = deviceList.get(index);
            }
            else {
                returnDevice = null;
            }
        }
        return returnDevice;
    }


    public RoomHubDevice GetAlljoynDevice(int index)
    {
        RoomHubDevice ret;
        synchronized(deviceList)
        {
            ret = deviceList.get(index);
        }
        return ret;
    }

    public void UpdateDeviceNewName(RoomHubDevice dev)
    {
        synchronized(deviceList)
        {
            int index = deviceList.indexOf(dev);
            if(index >= 0)
            {
                deviceList.get(index).setName(dev.getName());
            }

        }
    }

    public void UpdateDeviceSensorInfo(RoomHubDevice dev)
    {
        synchronized(deviceList)
        {
            int index = deviceList.indexOf(dev);
            if(index >= 0)
            {
                deviceList.get(index).UpdateSensor(dev);
            }
        }
    }
    public void UpdateDeviceNewName(int index,String name)
    {
        synchronized(deviceList)
        {
            deviceList.get(index).setName(name);
        }
    }
    public void UpdateDeviceSensorInfo(int index,RoomHubDevice dev)
    {
        synchronized(deviceList)
        {
            deviceList.get(index).UpdateSensor(dev);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RoomHubDevice> GetClone()
    {
        ArrayList<RoomHubDevice> ret;
        synchronized(deviceList)
        {
            ret = (ArrayList<RoomHubDevice>) deviceList.clone();

//			for(int i=0 ;i<ret.size();i++)
//			{
//				ret.get(i).setmInterface(null);
//			}
        }
        return ret;
    }

    public void RemoveAll() {
        synchronized(deviceList)
        {
            deviceList.clear();
        }
    }

}
