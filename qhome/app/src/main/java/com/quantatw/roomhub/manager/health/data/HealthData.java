package com.quantatw.roomhub.manager.health.data;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.ModifyDeviceNameReqPack;

/**
 * Created by erin on 4/28/16.
 */
public class HealthData extends BaseData implements HealthDeviceAction{

    public HealthData(int type) {
        super(type);
    }

    public boolean isUpdateOnLineStatus(RoomHubDevice device){
        boolean new_online=getOnLineStatus(device);
        boolean isOnLine = mOnlineStatus==1?true:false;
        if(isOnLine != new_online){
            mOnlineStatus =  new_online==true?1:0;
            return true;
        }

        return false;
    }

    public boolean isUpdateDeviceName(RoomHubDevice roomHubDevice) {
        String newName = getCurrentName(roomHubDevice);
        if(TextUtils.isEmpty(newName))
            return false;

        if(!TextUtils.isEmpty(newName) && !newName.equals(mDeviceName)) {
            mDeviceName = newName;
            return true;
        }
        return false;
    }

    private boolean getOnLineStatus(RoomHubDevice device){
        if (IsAlljoyn()) {
            //always is on line if alljoyn is exist
            return true;
        }else if (IsCloud() && (device.getExtraInfo() != null)) {
            //get cloud isOnlineStatus if the alljoyn doesn't exist
            return device.getExtraInfo().isOnlineStatus();
        }
        return false;
    }

    private String getCurrentName(RoomHubDevice device) {
        String name="";

        if(IsAlljoyn()) {
            name = device.getName();
        }else if(IsCloud()){
            if(device.getExtraInfo() != null)
                name = device.getExtraInfo().getDevice_name();
        }

        return name;
    }

    @Override
    public int rename(String newName) {
        ModifyDeviceNameReqPack req = new ModifyDeviceNameReqPack();
        req.setDeviceName(newName);
        BaseResPack res = CloudApi.getInstance().ModifyDeviceName(this.getUuid(),req);
//        if(res.getStatus_code() == ErrorKey.Success)
//            mDeviceName = newName;
        return res.getStatus_code();
    }

    @Override
    public int update(HealthData healthData) {
        return 0;
    }

    /**
     * Flags for special marshaling
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Write the concrete strategy to the Parcel.
     */
    public void writeToParcel(Parcel out, int flags) {
        // Serialize "this", so that we can get it back after IPC
        out.writeSerializable(this);
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<HealthData> CREATOR = new Parcelable.Creator<HealthData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public HealthData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (HealthData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public HealthData[] newArray(int size) {
            return new HealthData[size];
        }
    };
}
