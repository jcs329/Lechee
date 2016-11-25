package com.quantatw.sls.pack.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class DeviceUserReqPack extends BaseReqPack {
    private static final long serialVersionUID = -3620342324056304552L;

    private String userId;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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
    public static final Parcelable.Creator<DeviceUserReqPack> CREATOR = new Parcelable.Creator<DeviceUserReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public DeviceUserReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (DeviceUserReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public DeviceUserReqPack[] newArray(int size) {
            return new DeviceUserReqPack[size];
        }
    };
}
