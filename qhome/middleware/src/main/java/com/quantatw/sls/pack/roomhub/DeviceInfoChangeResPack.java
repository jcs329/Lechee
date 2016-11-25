package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.pack.base.BaseResPack;

public class DeviceInfoChangeResPack extends BaseResPack {
    private static final long serialVersionUID = -7684538134525204427L;

    private RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss devicInfo;

    public RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss getDeviceInfo() {
        return devicInfo;
    }
    public void setDeviceInfo(RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss devicInfo) {
        this.devicInfo = devicInfo;
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
    public static final Creator<DeviceInfoChangeResPack> CREATOR = new Parcelable.Creator<DeviceInfoChangeResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public DeviceInfoChangeResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (DeviceInfoChangeResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public DeviceInfoChangeResPack[] newArray(int size) {
            return new DeviceInfoChangeResPack[size];
        }
    };

}
